package com.example.padelbackend.service;

import com.example.padelbackend.model.Utilisateur;
import com.example.padelbackend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur getUtilisateurByMatricule(String matricule) {
        if (matricule == null || matricule.isEmpty()) {
            throw new IllegalArgumentException("Matricule cannot be null or empty");
        } else  {
            return utilisateurRepository.findByMatricule(matricule);
        }
    }
}
