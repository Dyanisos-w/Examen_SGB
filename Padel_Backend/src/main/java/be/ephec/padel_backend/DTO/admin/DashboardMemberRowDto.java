package be.ephec.padel_backend.DTO.admin;

import java.time.LocalDate;

public record DashboardMemberRowDto(
        String matricule,
        String nom,
        String prenom,
        Integer siteId,
        String siteNom,
        LocalDate interditReservationJusqua
) {
}

