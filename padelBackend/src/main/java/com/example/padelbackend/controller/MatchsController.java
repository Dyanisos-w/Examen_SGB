package com.example.padelbackend.controller;

import com.example.padelbackend.model.Matchs;
import com.example.padelbackend.service.MatchsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matchs")
public class MatchsController {
    private final MatchsService matchsService;
    public MatchsController(MatchsService matchsService) {
        this.matchsService = matchsService;
    }

    @PostMapping
    public ResponseEntity<String> createMatch(@RequestBody Matchs match) {
        matchsService.creerMatch(match);
        return ResponseEntity.ok("Match créé avec succès");
    }

    @GetMapping

    public List<Matchs> getAllMatchs() {
        return matchsService.getAllMatchs();
    }
    @GetMapping("/{id}")
    public Matchs getMatchById(@PathVariable Integer id) {
        return matchsService.getMatchById(id);
    }

}
