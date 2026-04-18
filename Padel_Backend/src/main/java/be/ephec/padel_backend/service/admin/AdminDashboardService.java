package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.DTO.admin.DashboardOverviewDto;
import be.ephec.padel_backend.DTO.admin.DashboardMemberRowDto;
import be.ephec.padel_backend.DTO.admin.DashboardReservationRowDto;
import be.ephec.padel_backend.DTO.admin.PeriodFilterDto;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.SiteOpeningHours;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.PaymentRepository;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SiteRepository siteRepository;
    private final SiteOpeningHoursRepository siteOpeningHoursRepository;
    private final UtilisateurRepository utilisateurRepository;

    private static final LocalTime DEFAULT_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CLOSING_TIME = LocalTime.of(22, 0);

    public DashboardOverviewDto getOverview(AdminAccessService.AdminScope scope, String period) {
        PeriodFilterDto filter = PeriodFilterDto.from(period);

        if (scope.global()) {
            return getOverviewAllSites(filter);
        }

        return getOverviewBySite(scope.siteId(), filter);
    }

    public List<DashboardReservationRowDto> getReservations(AdminAccessService.AdminScope scope, String period) {
        PeriodFilterDto filter = PeriodFilterDto.from(period);
        LocalDate start = filter.getStartDate();
        LocalDate end = filter.getEndDate();

        List<Reservation> reservations = scope.global()
                ? reservationRepository.findBetween(start, end)
                : reservationRepository.findBetween(start, end, scope.siteId());

        return reservations.stream().map(this::toReservationRow).toList();
    }

    public List<DashboardMemberRowDto> getMembers(AdminAccessService.AdminScope scope) {
        List<Utilisateur> members = scope.global()
                ? utilisateurRepository.findMembers()
                : utilisateurRepository.findMembersBySiteId(scope.siteId());

        return members.stream().map(this::toMemberRow).toList();
    }

    private DashboardOverviewDto getOverviewAllSites(PeriodFilterDto filter) {
        LocalDate start = filter.getStartDate();
        LocalDate end = filter.getEndDate();

        long totalReservations = reservationRepository.countBetween(start, end);
        long cancelledReservations = reservationRepository.countCancelledBetween(start, end);
        long activeReservations = Math.max(0, totalReservations - cancelledReservations);
        int totalPlayers = reservationRepository.countDistinct(start, end);
        double totalRevenue = paymentRepository.sumBetween(start, end);
        long availableSlots = computeAvailableSlotsAllSites(start, end);
        double occupancyRate = toRate(activeReservations, availableSlots);
        double cancellationRate = toRate(cancelledReservations, totalReservations);

        return new DashboardOverviewDto(totalReservations, totalRevenue, totalPlayers, occupancyRate, cancellationRate);
    }

    private DashboardOverviewDto getOverviewBySite(Integer siteId, PeriodFilterDto filter) {
        LocalDate start = filter.getStartDate();
        LocalDate end = filter.getEndDate();

        long totalReservations = reservationRepository.countBetween(start, end, siteId);
        long cancelledReservations = reservationRepository.countCancelledBetween(start, end, siteId);
        long activeReservations = Math.max(0, totalReservations - cancelledReservations);
        int totalPlayers = reservationRepository.countDistinct(start, end, siteId);
        double totalRevenue = paymentRepository.sumBetween(start, end, siteId);
        long availableSlots = computeAvailableSlotsBySite(siteId, start, end);
        double occupancyRate = toRate(activeReservations, availableSlots);
        double cancellationRate = toRate(cancelledReservations, totalReservations);

        return new DashboardOverviewDto(totalReservations, totalRevenue, totalPlayers, occupancyRate, cancellationRate);
    }

    private long computeAvailableSlotsAllSites(LocalDate start, LocalDate end) {
        long availableSlots = 0;
        for (Site site : siteRepository.findAll()) {
            availableSlots += computeAvailableSlotsBySite(site.getSiteId(), start, end);
        }
        return availableSlots;
    }

    private long computeAvailableSlotsBySite(Integer siteId, LocalDate start, LocalDate end) {
        Site site = siteRepository.findById(siteId).orElse(null);
        if (site == null || site.getNombreTerrains() <= 0) {
            return 0;
        }

        Map<DayOfWeek, SiteOpeningHours> openingHoursByDay = toOpeningHoursByDay(siteId);
        long slotsPerTerrain = 0;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            slotsPerTerrain += computeSlotsForDay(openingHoursByDay.get(date.getDayOfWeek()));
        }

        return slotsPerTerrain * site.getNombreTerrains();
    }

    private Map<DayOfWeek, SiteOpeningHours> toOpeningHoursByDay(Integer siteId) {
        Map<DayOfWeek, SiteOpeningHours> openingHoursByDay = new EnumMap<>(DayOfWeek.class);
        List<SiteOpeningHours> openingHours = siteOpeningHoursRepository.findBySiteSiteId(siteId);
        for (SiteOpeningHours dayConfig : openingHours) {
            openingHoursByDay.put(dayConfig.getDayOfWeek(), dayConfig);
        }
        return openingHoursByDay;
    }

    private long computeSlotsForDay(SiteOpeningHours openingHours) {
        if (openingHours != null && openingHours.isClosed()) {
            return 0;
        }

        LocalTime opening = openingHours != null && openingHours.getOpeningTime() != null
                ? openingHours.getOpeningTime()
                : DEFAULT_OPENING_TIME;
        LocalTime closing = openingHours != null && openingHours.getClosingTime() != null
                ? openingHours.getClosingTime()
                : DEFAULT_CLOSING_TIME;

        long slots = 0;
        LocalTime cursor = opening;
        while (!cursor.plusMinutes(90).isAfter(closing)) {
            slots++;
            cursor = cursor.plusMinutes(105);
        }

        return slots;
    }

    private double toRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }

        double rate = (numerator * 100.0) / denominator;
        return Math.max(0.0, Math.min(100.0, rate));
    }

    private DashboardReservationRowDto toReservationRow(Reservation reservation) {
        return new DashboardReservationRowDto(
                reservation.getIdReservation(),
                reservation.getDateReservation(),
                reservation.getHeureDebut(),
                reservation.getHeureFin(),
                reservation.getStatut(),
                reservation.getTypeReservation(),
                reservation.getMontantTotal(),
                reservation.getUtilisateur().getMatricule(),
                reservation.getUtilisateur().getNom() + " " + reservation.getUtilisateur().getPrenom(),
                reservation.getTerrain().getTerrainId(),
                reservation.getTerrain().getNom(),
                reservation.getTerrain().getSite().getSiteId(),
                reservation.getTerrain().getSite().getNom()
        );
    }

    private DashboardMemberRowDto toMemberRow(Utilisateur utilisateur) {
        Integer siteId = utilisateur.getSiteAssociated() != null ? utilisateur.getSiteAssociated().getSiteId() : null;
        String siteNom = utilisateur.getSiteAssociated() != null ? utilisateur.getSiteAssociated().getNom() : null;

        return new DashboardMemberRowDto(
                utilisateur.getMatricule(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                siteId,
                siteNom,
                utilisateur.getInterditReservationJusqua()
        );
    }
}
