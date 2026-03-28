package be.ephec.padel_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Reservation_Utilisateur")
@IdClass(ReservationUtilisateurId.class)
public class ReservationUtilisateur {

    @Id
    @ManyToOne
    @JoinColumn(name = "ReservationID")
    private Reservation reservation;

    @Id
    @ManyToOne
    @JoinColumn(name = "UtilisateurMatricule")
    private Utilisateur utilisateur;

    @Column(name = "StatutResaUser")
    private String statutResaUser;
    @Column(name = "montant_du")
    private Double montantDu;

    @Column(name = "montant_paye")
    private Double montantPaye;

    @Column(name = "statut_paiement")
    private String statutPaiement;
    public ReservationUtilisateur() {}

    /** Constructeur pratique utilisé par ReservationEngine */
    public ReservationUtilisateur(Reservation reservation, Utilisateur utilisateur) {
        this.reservation = reservation;
        this.utilisateur = utilisateur;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public String getStatutResaUser() {
        return statutResaUser;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setStatutResaUser(String statutResaUser) {
        this.statutResaUser = statutResaUser;
    }

    public Double getMontantDu() {
        return montantDu;
    }

    public void setMontantDu(Double montantDu) {
        this.montantDu = montantDu;
    }

    public Double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(Double montantPaye) {
        this.montantPaye = montantPaye;
    }

    public String getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
    }
}
