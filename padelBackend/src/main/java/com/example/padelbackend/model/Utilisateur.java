package com.example.padelbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Utilisateur")
public class Utilisateur {

    @Id
    @Column(name = "Matricule")
    private String matricule;
    
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    @Column(name = "Nom")
    private String nom;
    
    @Column(name = "Prenom")
    private String prenom;

    public Utilisateur() {
    }

    public Utilisateur(String matricule, String nom, String prenom, String password) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
    }

    public String getMatricule() { return matricule; }
    public String getPassword() { return password; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public void setPassword(String password) { this.password = password; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
}
