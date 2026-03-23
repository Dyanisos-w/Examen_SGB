package be.ephec.padel_backend.model;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;
@Entity
@Table(name = "Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDReservation")
    private Integer idReservation;

    @ManyToOne
    @JoinColumn(name = "UtilisateurMatricule", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "TerrainID", nullable = false)
    private Terrain terrain;

    @Column(name = "date_reservation", nullable = false)
    private LocalDate dateReservation;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "statut")
    private String statut;

    @Column(name = "est_maintenu")
    private Boolean estMaintenu;

    @Column(name = "est_complet")
    private Boolean estComplet;

    public Reservation() {}

    public Integer getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(Integer idReservation) {
        this.idReservation = idReservation;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
        this.dateReservation = dateReservation;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Boolean getEstMaintenu() {
        return estMaintenu;
    }

    public void setEstMaintenu(Boolean estMaintenu) {
        this.estMaintenu = estMaintenu;
    }

    public Boolean getEstComplet() {
        return estComplet;
    }

    public void setEstComplet(Boolean estComplet) {
        this.estComplet = estComplet;
    }
// getters setters
}