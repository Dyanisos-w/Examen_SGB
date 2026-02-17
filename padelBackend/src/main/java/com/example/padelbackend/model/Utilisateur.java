package com.example.padelbackend.model;

public class Utilisateur {

    private String matricule;
    private String password;
    private String nom;
    private String prenom;

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
    public String setMatricule(String matricule) { this.matricule = matricule; return matricule; }
    public String setPassword(String password) { this.password = password; return password; }
}
