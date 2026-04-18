package be.ephec.padel_backend.DTO;

public class PublicReservationDto {
    public Integer reservationId;
    public Integer terrainId;
    public String dateHeure;    // ISO datetime, e.g. "2024-04-15T14:00:00"
    public String siteNom;
    public String terrainNom;
    public int nbJoueurs;
    public String statut;
}

