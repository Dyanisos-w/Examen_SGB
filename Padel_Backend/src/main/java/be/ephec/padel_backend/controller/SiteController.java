package be.ephec.padel_backend.controller;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.repository.SiteRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/sites", produces = "application/json")
public class SiteController {
    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @GetMapping(produces = "application/json")
    public List<SitePublicDto> getAllSites() {
        return siteRepository.findAll().stream()
                .map(site -> new SitePublicDto(site.getSiteId(), site.getNom()))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public SitePublicDto getSite(@PathVariable Integer id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site non trouve"));
        return new SitePublicDto(site.getSiteId(), site.getNom());
    }

    @PostMapping
    @PreAuthorize("hasRole('GLOBALADMIN')")
    public Site createSite(@RequestBody Site site) {
        // Les terrains sont geres via un endpoint dedie.
        site.setNombreTerrains(0);
        return siteRepository.save(site);
    }

    public record SitePublicDto(Integer siteId, String nom) {
    }
}
