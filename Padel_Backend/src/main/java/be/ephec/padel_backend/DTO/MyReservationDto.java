package be.ephec.padel_backend.DTO;

import java.util.List;

public class MyReservationDto {
    public Integer reservationId;
    public Integer terrainId;
    public String dateHeure;           // ISO datetime, e.g. "2024-04-15T14:00:00"
    public String siteNom;
    public String terrainNom;
    public String typeReservation;     // "PUBLIC" or "PRIVATE"
    public String statutReservation;
    public List<ParticipantPaymentDto> participants;
    public boolean isOrganizer;
}

