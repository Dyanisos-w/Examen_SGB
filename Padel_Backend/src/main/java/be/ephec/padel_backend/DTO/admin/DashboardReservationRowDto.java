package be.ephec.padel_backend.DTO.admin;

import java.time.LocalDate;
import java.time.LocalTime;

public record DashboardReservationRowDto(
        Integer reservationId,
        LocalDate dateReservation,
        LocalTime heureDebut,
        LocalTime heureFin,
        String statut,
        String typeReservation,
        Double montantTotal,
        String utilisateurMatricule,
        String utilisateurNom,
        Integer terrainId,
        String terrainNom,
        Integer siteId,
        String siteNom
) {
}

