package com.example.padelbackend.controller;

import com.example.padelbackend.model.Utilisateur;
import com.example.padelbackend.service.UtilisateurService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/utilisateur")
public class UtilisateurController {
private  final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/{matricule}")

    public ResponseEntity<Utilisateur> getUtilisateur(@PathVariable String matricule)
    {
        return ResponseEntity.ok(utilisateurService.findByMatricule(matricule));
    }
}
