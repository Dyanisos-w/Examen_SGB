package be.ephec.padel_backend.DTO.admin;

import java.time.LocalDate;

public record SiteClosureAdminRequestDto(
        Integer siteId,
        boolean applyToAll,
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {
}

