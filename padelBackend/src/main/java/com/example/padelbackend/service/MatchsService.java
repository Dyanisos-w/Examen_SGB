package com.example.padelbackend.service;

import com.example.padelbackend.model.Matchs;
import com.example.padelbackend.repository.MatchsRepository;
import com.example.padelbackend.repository.SiteRepository;
import com.example.padelbackend.repository.TerrainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchsService {
    private final MatchsRepository matchsRepository;
    private final SiteRepository siteRepository;
    private final TerrainRepository terrainRepository;

    public MatchsService(MatchsRepository matchsRepository,
                         SiteRepository siteRepository,
                         TerrainRepository terrainRepository) {
        this.matchsRepository = matchsRepository;
        this.siteRepository = siteRepository;
        this.terrainRepository = terrainRepository;
    }

    public void creerMatch(Matchs match) {

        if (match == null
                || match.getSiteId() == null
                || match.getReservationId() == null
                || match.getTerrainId() == null) {
            throw new IllegalArgumentException("All foreign keys must be provided");
        }

        // Vérifier que le site existe
        siteRepository.getSiteById(match.getSiteId());

        // Vérifier que le terrain existe
        terrainRepository.getTerrainById(match.getTerrainId());

        matchsRepository.createMatch(match);
    }

    public List<Matchs> getAllMatchs() {
        return matchsRepository.getAllMatchs();
    }

    public Matchs getMatchById(Integer id) {
        return matchsRepository.getMatchById(id);
    }
}
