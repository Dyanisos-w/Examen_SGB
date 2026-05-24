package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.DTO.admin.SiteClosureAdminRequestDto;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteClosureService {
    private final SiteClosureRepository siteClosureRepository;
    private final SiteRepository siteRepository;

    /**
     * Vérifie si un site est fermé à une date donnée (fermeture globale ou spécifique)
     */
    public boolean isSiteClosedOnDate(Site site, LocalDate date) {
        boolean globalClosed = !siteClosureRepository.findGlobalClosuresOnDate(date).isEmpty();
        boolean siteClosed = !siteClosureRepository.findSiteClosuresOnDate(site, date).isEmpty();
        return globalClosed || siteClosed;
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
            createClosure(loadSite(scope.siteId()), request);
            return;
        }

        if (request.applyToAll()) {
            if (request.siteId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "siteId ne peut pas etre fourni avec applyToAll=true");
            }
            createClosure(null, request);
            return;
        }

        if (request.siteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "siteId ou applyToAll=true est requis pour un GLOBALADMIN");
        }

        createClosure(loadSite(request.siteId()), request);
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

    private void createClosure(Site site, SiteClosureAdminRequestDto request) {
        SiteClosure closure = new SiteClosure();
        closure.setSite(site);
        closure.setDateDebut(request.startDate());
        closure.setDateFin(request.endDate());
        closure.setMotif(normalizeReason(request.reason()));
        siteClosureRepository.save(closure);
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }

        String normalized = reason.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

