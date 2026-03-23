package be.ephec.padel_backend.model;
import jakarta.persistence.*;
@Entity
@Table(name = "Site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Integer siteId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "adresse", nullable = false)
    private String adresse;

    @Column(name = "nombre_terrains")
    private int nombreTerrains;

    public Site() {}

    public Site(String nom, String adresse) {

        this.nom = nom;
        this.adresse = adresse;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public int getNombreTerrains() { return nombreTerrains; }

    /** Alias métier utilisé par ReservationEngine */
    public int getNumberOfCourts() { return nombreTerrains; }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setNombreTerrains(int nombreTerrains) { this.nombreTerrains = nombreTerrains; }

    public Terrain[] getTerrains() {
        return null;
    }
}
