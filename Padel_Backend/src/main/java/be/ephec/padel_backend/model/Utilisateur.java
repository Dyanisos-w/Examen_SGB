package be.ephec.padel_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Utilisateur")
public class Utilisateur {

    @Id
    @Column(name = "matricule", nullable = false, unique = true)
    private String matricule;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "penalite_montant")
    private BigDecimal penaliteMontant;

    @Column(name = "interdit_reservation_jusqua")
    private LocalDate interditReservationJusqua;

    @Column (name = "password")
    private String password;

    // Relation vers Site
    @ManyToOne
    @JoinColumn(name = "site_associated")
    private Site siteAssociated;

    public Utilisateur() {}

    public Utilisateur(String matricule, String nom, String prenom) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getMatricule() {
        return matricule;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public BigDecimal getPenaliteMontant() {
        return penaliteMontant;
    }

    public LocalDate getInterditReservationJusqua() {
        return interditReservationJusqua;
    }

    public Site getSiteAssociated() {
        return siteAssociated;
    }

    public void setPenaliteMontant(BigDecimal penaliteMontant) {
        this.penaliteMontant = penaliteMontant;
    }

    public void setInterditReservationJusqua(LocalDate interditReservationJusqua) {
        this.interditReservationJusqua = interditReservationJusqua;
    }

    public void setSiteAssociated(Site siteAssociated) {
        this.siteAssociated = siteAssociated;
    }

    // Alias pour ReservationEngine
    public LocalDate getPenaltyEndDate() {
        return interditReservationJusqua;
    }

    public void setPenaltyEndDate(LocalDate date) {
        this.interditReservationJusqua = date;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}