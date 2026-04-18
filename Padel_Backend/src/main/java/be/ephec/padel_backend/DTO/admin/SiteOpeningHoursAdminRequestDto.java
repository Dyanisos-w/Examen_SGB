package be.ephec.padel_backend.DTO.admin;

import java.util.List;

public record SiteOpeningHoursAdminRequestDto(
        List<SiteOpeningHoursAdminDayDto> days
) {
}

