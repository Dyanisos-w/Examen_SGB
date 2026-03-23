package be.ephec.padel_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "Payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ReservationIDReservation", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "UtilisateurMatricule", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "Montant", nullable = false)
    private BigDecimal montant;

    @Column(name = "DatePaiement")
    private LocalDate datePaiement;

    @Column(name = "StatutPaiement")
    private String statutPaiement;

    public Payment() {}

    /** Constructeur pratique utilisé par ReservationEngine */
    public Payment(Utilisateur utilisateur, BigDecimal montant, Reservation reservation) {
        this.utilisateur = utilisateur;
        this.montant = montant;
        this.reservation = reservation;
    }

    public Integer getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public BigDecimal getMontant() { return montant; }
    public LocalDate getDatePaiement() { return datePaiement; }
    public String getStatutPaiement() { return statutPaiement; }

    public void setReservation(Reservation reservation) { this.reservation = reservation; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement;}

}
