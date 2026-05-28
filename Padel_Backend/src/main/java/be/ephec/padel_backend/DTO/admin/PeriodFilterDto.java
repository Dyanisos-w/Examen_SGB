package be.ephec.padel_backend.DTO.admin;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class PeriodFilterDto {

    private LocalDate startDate;
    private LocalDate endDate;

    public PeriodFilterDto(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public static PeriodFilterDto from(String period)
    {
        LocalDate today = LocalDate.now();

        return switch (period.toLowerCase()) {
            case "today"        -> new PeriodFilterDto(today.minusDays(1), today);
            case "7d"           -> new PeriodFilterDto(today.minusDays(7), today);
            case "week"         -> new PeriodFilterDto(today.with(java.time.DayOfWeek.MONDAY), today.with(java.time.DayOfWeek.SUNDAY));
            case "month"        -> new PeriodFilterDto(today.minusMonths(1), today);
            case "currentmonth" -> new PeriodFilterDto(today.withDayOfMonth(1), today);
            case "next30d"      -> new PeriodFilterDto(today, today.plusDays(30));
            case "year"         -> new PeriodFilterDto(today.minusYears(1), today);
            default -> throw new IllegalArgumentException("Période non reconnue : " + period);
        };
    }

}
