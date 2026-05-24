package be.ephec.padel_backend.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import be.ephec.padel_backend.DTO.admin.SiteAdminResponseDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/admin/sites")
@RequiredArgsConstructor
public class AdminSiteManagementController {

    private final AdminAccessService adminAccessService;
    private final SiteRepository siteRepository;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<SiteAdminResponseDto> createSite(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestBody CreateSiteRequest createSiteRequest) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        if (!scope.global()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul GLOBALADMIN peut creer un site");
        }

        if (isBlank(createSiteRequest.nom()) || isBlank(createSiteRequest.adresse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom et adresse sont obligatoires");
        }

        Site site = new Site();
        site.setNom(HtmlUtils.htmlEscape(createSiteRequest.nom().trim()));
        site.setAdresse(HtmlUtils.htmlEscape(createSiteRequest.adresse().trim()));
        site.setNombreTerrains(0);

        Site saved = siteRepository.save(site);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SiteAdminResponseDto(saved.getSiteId(), saved.getNom(), saved.getAdresse(), saved.getNombreTerrains()));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateSiteRequest(String nom, String adresse) {
    }
}

