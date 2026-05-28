package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.DTO.admin.SiteClosureAdminRequestDto;
import be.ephec.padel_backend.DTO.admin.SiteClosureAdminResponseDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.SiteClosure;
import be.ephec.padel_backend.repository.SiteClosureRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteClosureService {
    private final SiteClosureRepository siteClosureRepository;
    private final SiteRepository siteRepository;

    /**
     * Vérifie si un site est fermé à une date donnée
     */
    public boolean isSiteClosedOnDate(Site site, LocalDate date) {
        // Fermetures propres au site (inclut celles marquées global rattachées au site)
        boolean siteClosed = !siteClosureRepository.findSiteClosuresOnDate(site, date).isEmpty();
        // Compat ancienne donnée : fermetures globales stockées sans site (site IS NULL)
        boolean legacyClosed = !siteClosureRepository.findLegacyGlobalClosuresOnDate(date).isEmpty();
        return siteClosed || legacyClosed;
    }

    /**
     * Récupère toutes les fermetures (globales ou site) sur une période
     */
    public List<SiteClosure> getClosuresForSiteAndPeriod(Site site, LocalDate start, LocalDate end) {
        return siteClosureRepository.findClosuresForSiteAndPeriod(site, start, end);
    }

    @Transactional
    public void applyClosure(AdminAccessService.AdminScope scope, SiteClosureAdminRequestDto request) {
        validate(request);

        if (!scope.global()) {
            if (scope.siteId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun site associe a ce compte LOCALADMIN");
            }
            Site site = loadSite(scope.siteId());
            checkNoDuplicate(site, request);
            createClosure(site, request, false);
            return;
        }

        if (request.applyToAll()) {
            if (request.siteId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "siteId ne peut pas etre fourni avec applyToAll=true");
            }
            List<Site> allSites = siteRepository.findAll();
            // Vérifie les doublons avant toute insertion (transaction atomique)
            List<String> conflicts = new ArrayList<>();
            for (Site site : allSites) {
                if (siteClosureRepository.existsBySiteAndDateDebutAndDateFin(site, request.startDate(), request.endDate())) {
                    conflicts.add(site.getNom());
                }
            }
            if (!conflicts.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Une fermeture identique existe déjà pour ces sites : " + String.join(", ", conflicts));
            }
            for (Site site : allSites) {
                createClosure(site, request, true);
            }
            return;
        }

        if (request.siteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "siteId ou applyToAll=true est requis pour un GLOBALADMIN");
        }

        Site site = loadSite(request.siteId());
        checkNoDuplicate(site, request);
        createClosure(site, request, false);
    }

    private void checkNoDuplicate(Site site, SiteClosureAdminRequestDto request) {
        if (siteClosureRepository.existsBySiteAndDateDebutAndDateFin(site, request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une fermeture identique existe déjà pour ce site sur cette période.");
        }
    }

    private void validate(SiteClosureAdminRequestDto request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La requete est obligatoire");
        }

        if (request.startDate() == null || request.endDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Les dates de debut et de fin sont obligatoires");
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de debut doit etre inferieure ou egale a la date de fin");
        }
    }

    private Site loadSite(Integer siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouve : " + siteId));
    }

    private void createClosure(Site site, SiteClosureAdminRequestDto request, boolean isGlobal) {
        SiteClosure closure = new SiteClosure();
        closure.setSite(site);
        closure.setDateDebut(request.startDate());
        closure.setDateFin(request.endDate());
        closure.setMotif(normalizeReason(request.reason()));
        closure.setGlobal(isGlobal);
        siteClosureRepository.save(closure);
    }

    public List<SiteClosureAdminResponseDto> getGlobalClosures(AdminAccessService.AdminScope scope) {
        if (!scope.global()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Réservé au GLOBALADMIN");
        }
        // Déduplique par (dateDebut, dateFin) car chaque fermeture globale est stockée une fois par site
        Map<String, SiteClosureAdminResponseDto> deduplicated = new LinkedHashMap<>();
        siteClosureRepository.findByGlobalTrueOrderByDateDebutAsc().forEach(c -> {
            String key = c.getDateDebut() + "|" + c.getDateFin();
            deduplicated.putIfAbsent(key,
                    new SiteClosureAdminResponseDto(c.getId(), c.getDateDebut(), c.getDateFin(), c.getMotif(), true));
        });
        return new ArrayList<>(deduplicated.values());
    }

    public List<SiteClosureAdminResponseDto> getSiteClosures(AdminAccessService.AdminScope scope, Integer requestedSiteId) {
        Integer siteId;
        if (scope.global()) {
            if (requestedSiteId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "siteId requis pour un GLOBALADMIN");
            }
            siteId = requestedSiteId;
        } else {
            if (requestedSiteId != null && !requestedSiteId.equals(scope.siteId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce compte ne peut consulter que son propre site");
            }
            siteId = scope.siteId();
        }
        // Retourne toutes les fermetures du site (global=true et global=false)
        return siteClosureRepository.findBySite_SiteIdOrderByDateDebutAsc(siteId)
                .stream()
                .map(c -> new SiteClosureAdminResponseDto(c.getId(), c.getDateDebut(), c.getDateFin(), c.getMotif(), c.isGlobal()))
                .toList();
    }

    @Transactional
    public void deleteClosure(AdminAccessService.AdminScope scope, Long id) {
        if (!scope.global()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Réservé au GLOBALADMIN");
        }
        SiteClosure closure = siteClosureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fermeture introuvable"));
        if (closure.isGlobal()) {
            // Supprime toutes les occurrences de cette fermeture globale (une par site)
            siteClosureRepository.deleteAllGlobalClosuresForPeriod(closure.getDateDebut(), closure.getDateFin());
        } else {
            siteClosureRepository.delete(closure);
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }

        String normalized = reason.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

