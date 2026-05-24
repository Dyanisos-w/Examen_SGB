package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.TerrainAdminResponseDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/admin/terrains")
@RequiredArgsConstructor
public class AdminTerrainManagementController {

    private final AdminAccessService adminAccessService;
    private final TerrainRepository terrainRepository;
    private final SiteRepository siteRepository;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TerrainAdminResponseDto> createTerrain(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestBody CreateTerrainRequest createTerrainRequest) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);

        Integer effectiveSiteId = resolveEffectiveSiteId(scope, createTerrainRequest.siteId());
        if (isBlank(createTerrainRequest.nom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du terrain est obligatoire");
        }

        Site site = siteRepository.findById(effectiveSiteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site introuvable"));

        String terrainName = HtmlUtils.htmlEscape(createTerrainRequest.nom().trim());
        if (terrainRepository.existsBySiteSiteIdAndNomIgnoreCase(effectiveSiteId, terrainName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un terrain avec ce nom existe deja sur ce site");
        }

        Terrain terrain = new Terrain();
        terrain.setNom(terrainName);
        terrain.setSite(site);

        Terrain saved = terrainRepository.save(terrain);
        site.setNombreTerrains(site.getNombreTerrains() + 1);
        siteRepository.save(site);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TerrainAdminResponseDto(
                        saved.getTerrainId(),
                        saved.getNom(),
                        new TerrainAdminResponseDto.SiteInfo(site.getSiteId(), site.getNom())
                ));
    }

    @DeleteMapping(path = "/{terrainId}")
    @Transactional
    public ResponseEntity<Void> deleteTerrain(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @PathVariable Integer terrainId) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);

        Terrain terrain = terrainRepository.findById(terrainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terrain introuvable"));

        Site site = terrain.getSite();
        if (site == null || site.getSiteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site associe introuvable");
        }

        if (!scope.global()) {
            if (scope.siteId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun site associe a ce compte LOCALADMIN");
            }
            if (!scope.siteId().equals(site.getSiteId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "LOCALADMIN limite a son propre site");
            }
        }

        terrainRepository.delete(terrain);
        site.setNombreTerrains(Math.max(0, site.getNombreTerrains() - 1));
        siteRepository.save(site);

        return ResponseEntity.noContent().build();
    }

    private Integer resolveEffectiveSiteId(AdminAccessService.AdminScope scope, Integer requestedSiteId) {
        if (scope.global()) {
            if (requestedSiteId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le site est obligatoire pour un GLOBALADMIN");
            }
            return requestedSiteId;
        }

        if (scope.siteId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun site associe a ce compte LOCALADMIN");
        }

        if (requestedSiteId != null && !scope.siteId().equals(requestedSiteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "LOCALADMIN limite a son propre site");
        }

        return scope.siteId();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateTerrainRequest(String nom, Integer siteId) {
    }
}
