package be.ephec.padel_backend.DTO.admin;

import java.util.List;

public record SiteOpeningHoursAdminResponseDto(
        Integer siteId,
        String siteName,
        boolean configured,
        List<SiteOpeningHoursAdminDayDto> days
) {
}

