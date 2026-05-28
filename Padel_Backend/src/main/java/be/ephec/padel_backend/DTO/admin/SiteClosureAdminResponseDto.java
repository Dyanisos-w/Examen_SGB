package be.ephec.padel_backend.DTO.admin;

import java.time.LocalDate;

public record SiteClosureAdminResponseDto(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        boolean global
) {
}
