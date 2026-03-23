package be.ephec.padel_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Terrain")
public class Terrain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terrain_id")
    private Integer terrainId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    public Terrain() {}

    public Terrain(Integer terrainId, String nom, Site site) {
        this.terrainId = terrainId;
        this.nom = nom;
        this.site = site;
    }

    public Integer getTerrainId() {
        return terrainId;
    }

    public String getNom() {
        return nom;
    }

    public Site getSite() {
        return site;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
