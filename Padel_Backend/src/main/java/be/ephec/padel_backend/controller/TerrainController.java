package be.ephec.padel_backend.controller;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.repository.TerrainRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/terrains", produces = "application/json")
public class TerrainController {
    private final TerrainRepository terrainRepository;

    public TerrainController(TerrainRepository terrainRepository) {
        this.terrainRepository = terrainRepository;
    }

    @GetMapping(produces = "application/json")
    public List<TerrainPublicDto> getAllTerrains(@RequestParam(required = false) Integer siteId) {
        List<Terrain> terrains;
        if (siteId != null) {
            terrains = terrainRepository.findBySiteSiteId(siteId);
        } else {
            terrains = terrainRepository.findAll();
        }

        return terrains.stream()
                .map(terrain -> new TerrainPublicDto(
                        terrain.getTerrainId(),
                        terrain.getNom(),
                        terrain.getSite() != null ? terrain.getSite().getSiteId() : null
                ))
                .collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('GLOBALADMIN')")
    public Terrain createTerrain(@RequestBody Terrain ignoredTerrain) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Utiliser /api/admin/terrains pour maintenir la coherence des compteurs de site");
    }

    public record TerrainPublicDto(Integer terrainId, String nom, Integer siteId) {
    }
}
