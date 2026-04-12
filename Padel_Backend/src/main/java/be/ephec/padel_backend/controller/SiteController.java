package be.ephec.padel_backend.controller;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.repository.SiteRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sites")
public class SiteController {
    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @GetMapping
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Site getSite(@PathVariable Integer id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site non trouvé"));
    }

    @PostMapping
    @PreAuthorize("hasRole('GLOBALADMIN')")
    public Site createSite(@RequestBody Site site) {
        return siteRepository.save(site);
    }
}
