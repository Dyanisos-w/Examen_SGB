package be.ephec.padel_backend.DTO.admin;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record SiteOpeningHoursAdminDayDto(
        DayOfWeek dayOfWeek,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean closed
) {
}

