package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.DTO.admin.SiteOpeningHoursAdminDayDto;
import be.ephec.padel_backend.DTO.admin.SiteOpeningHoursAdminRequestDto;
import be.ephec.padel_backend.DTO.admin.SiteOpeningHoursAdminResponseDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.SiteOpeningHours;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteOpeningHoursAdminService {

    private static final LocalTime DEFAULT_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CLOSING_TIME = LocalTime.of(22, 0);

    private final SiteOpeningHoursRepository siteOpeningHoursRepository;
    private final SiteRepository siteRepository;

    public SiteOpeningHoursAdminResponseDto getOpeningHours(AdminAccessService.AdminScope scope, Integer requestedSiteId) {
        Integer siteId = resolveTargetSiteId(scope, requestedSiteId);
        Site site = loadSite(siteId);
        List<SiteOpeningHours> openingHours = siteOpeningHoursRepository.findBySiteSiteId(siteId);

        return toResponse(site, openingHours, !openingHours.isEmpty());
    }

    @Transactional
    public SiteOpeningHoursAdminResponseDto updateOpeningHours(AdminAccessService.AdminScope scope,
                                                               Integer requestedSiteId,
                                                               SiteOpeningHoursAdminRequestDto request) {
        Integer siteId = resolveTargetSiteId(scope, requestedSiteId);
        Site site = loadSite(siteId);
        Map<DayOfWeek, SiteOpeningHoursAdminDayDto> daysByWeek = validateAndIndex(request);

        siteOpeningHoursRepository.deleteBySiteSiteId(siteId);

        List<SiteOpeningHours> openingHours = Arrays.stream(DayOfWeek.values())
                .map(dayOfWeek -> toEntity(site, daysByWeek.get(dayOfWeek)))
                .toList();

        List<SiteOpeningHours> savedOpeningHours = siteOpeningHoursRepository.saveAll(openingHours);
        return toResponse(site, savedOpeningHours, true);
    }

    private Integer resolveTargetSiteId(AdminAccessService.AdminScope scope, Integer requestedSiteId) {
        if (scope.global()) {
            if (requestedSiteId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le siteId est requis pour un GLOBALADMIN");
            }
            return requestedSiteId;
        }

        if (requestedSiteId != null && !requestedSiteId.equals(scope.siteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce compte LOCALADMIN ne peut gérer qu'un seul site");
        }

        return scope.siteId();
    }

    private Site loadSite(Integer siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé : " + siteId));
    }

    private Map<DayOfWeek, SiteOpeningHoursAdminDayDto> validateAndIndex(SiteOpeningHoursAdminRequestDto request) {
        if (request == null || request.days() == null || request.days().size() != DayOfWeek.values().length) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les 7 jours de la semaine doivent être fournis");
        }

        Map<DayOfWeek, SiteOpeningHoursAdminDayDto> daysByWeek = new EnumMap<>(DayOfWeek.class);

        for (SiteOpeningHoursAdminDayDto day : request.days()) {
            if (day == null || day.dayOfWeek() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chaque jour doit préciser un dayOfWeek");
            }

            if (daysByWeek.putIfAbsent(day.dayOfWeek(), day) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chaque jour de la semaine doit être unique");
            }

            validateDay(day);
        }

        if (daysByWeek.size() != DayOfWeek.values().length) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La configuration doit couvrir les 7 jours");
        }

        return daysByWeek;
    }

    private void validateDay(SiteOpeningHoursAdminDayDto day) {
        if (day.closed()) {
            if (day.openingTime() != null || day.closingTime() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Un jour fermé ne doit pas contenir d'horaires d'ouverture ou de fermeture");
            }
            return;
        }

        if (day.openingTime() == null || day.closingTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un jour ouvert doit contenir une heure d'ouverture et une heure de fermeture");
        }

        if (!day.openingTime().isBefore(day.closingTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'heure d'ouverture doit être strictement avant l'heure de fermeture");
        }
    }

    private SiteOpeningHours toEntity(Site site, SiteOpeningHoursAdminDayDto dayDto) {
        SiteOpeningHours openingHours = new SiteOpeningHours();
        openingHours.setSite(site);
        openingHours.setDayOfWeek(dayDto.dayOfWeek());
        openingHours.setClosed(dayDto.closed());
        openingHours.setOpeningTime(dayDto.closed() ? null : dayDto.openingTime());
        openingHours.setClosingTime(dayDto.closed() ? null : dayDto.closingTime());
        return openingHours;
    }

    private SiteOpeningHoursAdminResponseDto toResponse(Site site,
                                                        List<SiteOpeningHours> openingHours,
                                                        boolean configured) {
        Map<DayOfWeek, SiteOpeningHours> dayToOpeningHours = new EnumMap<>(DayOfWeek.class);
        for (SiteOpeningHours openingHour : openingHours) {
            dayToOpeningHours.put(openingHour.getDayOfWeek(), openingHour);
        }

        List<SiteOpeningHoursAdminDayDto> days = Arrays.stream(DayOfWeek.values())
                .map(dayOfWeek -> toDayDto(dayOfWeek, dayToOpeningHours.get(dayOfWeek)))
                .toList();

        return new SiteOpeningHoursAdminResponseDto(site.getSiteId(), site.getNom(), configured, days);
    }

    private SiteOpeningHoursAdminDayDto toDayDto(DayOfWeek dayOfWeek, SiteOpeningHours openingHours) {
        if (openingHours == null) {
            return new SiteOpeningHoursAdminDayDto(dayOfWeek, DEFAULT_OPENING_TIME, DEFAULT_CLOSING_TIME, false);
        }

        return new SiteOpeningHoursAdminDayDto(
                dayOfWeek,
                openingHours.getOpeningTime(),
                openingHours.getClosingTime(),
                openingHours.isClosed()
        );
    }
}

