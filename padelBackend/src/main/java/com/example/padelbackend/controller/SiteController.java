package com.example.padelbackend.controller;

import com.example.padelbackend.model.Site;
import com.example.padelbackend.service.SiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class SiteController {
    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping
    public ResponseEntity<String> createSite(@RequestBody Site site) {

        siteService.creerSite(site);
        return ResponseEntity.ok("Site créé avec succès");
    }

    @GetMapping
    public List<Site> getAllSites() {
        return siteService.getAllSites();
    }

    @GetMapping("/{id}")
    public Site getSiteById(@PathVariable Integer id) {
        return siteService.getSiteById(id);
    }
}
