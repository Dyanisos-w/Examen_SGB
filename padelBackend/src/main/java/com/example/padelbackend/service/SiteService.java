package com.example.padelbackend.service;
import com.example.padelbackend.model.Site;
import com.example.padelbackend.repository.SiteRepository;
import org.springframework.stereotype.Service;
import  java.util.List;
import java.awt.*;

@Service
public class SiteService {
    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public void creerSite(Site site) {
        if (site == null
                || site.getNom() == null || site.getNom().isBlank()
                || site.getAdresse() == null || site.getAdresse().isBlank()
                || site.getHeureOuverture() == null
                || site.getHeureFermeture() == null) {
            throw new IllegalArgumentException("All fields must be provided and not empty");
        }

        siteRepository.createSite(site);
    }

    public List<Site> getAllSites() {
        return siteRepository.getAllSites();
    }
    public Site getSiteById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive integer");
        }
        return siteRepository.findById(id);
    }

}
