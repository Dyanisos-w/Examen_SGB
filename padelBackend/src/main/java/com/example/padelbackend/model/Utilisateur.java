package com.example.padelbackend.model;

import com.example.padelbackend.repository.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;

public class Utilisateur {
    private String matricule;
    private String Hash;

    public boolean seConnecter(String matricule, String Hash) {


        if (matricule.equals("G1234") && Hash.equals("password")) {
            return true;
        } else {
            return false;
        }
    }


    public void seDeconnecter(String matricule) {
        System.out.println("L'utilisateur " + matricule + " s'est déconnecté.");
    }

    public void reserverTerrain(String site, String terrain, String date, String heure) {
        System.out.println("Réservation du terrain " + terrain + " au site " + site + " pour le " + date + " à " + heure);
    }

    public void annulerReservation(String reservationId) {
        System.out.println("Annulation de la réservation avec l'ID : " + reservationId);
    }

    public void consulterReservations(String matricule) {
        System.out.println("Consultation des réservations pour l'utilisateur " + matricule);
    }

    public void register(String matricule, String nom, String prenom, String password) {
        try {
            UtilisateurRepository.createUtilisateur(matricule, nom, prenom, password);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("com.example.padelbackend.model.Utilisateur déjà existant");
        }
    }

    public Utilisateur login(String matricule, String password) {

        Utilisateur user = UtilisateurRepository.findByMatricule(matricule);

        if (user == null) {
            throw new RuntimeException("com.example.padelbackend.model.Utilisateur inexistant");
        }

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return user;
    }


}

