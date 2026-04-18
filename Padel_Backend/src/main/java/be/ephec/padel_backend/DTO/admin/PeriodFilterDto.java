package be.ephec.padel_backend.DTO.admin;

import java.time.LocalDate;

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
            case "today" -> new PeriodFilterDto(today.minusDays(1), today);
            case "7d" -> new PeriodFilterDto(today.minusDays(7), today);
            case "month" -> new PeriodFilterDto(today.minusMonths(1), today);
            case "currentmonth"  -> new PeriodFilterDto(today.withDayOfMonth(1), today);
            case "year" -> new PeriodFilterDto(today.minusYears(1), today);

            default -> throw new IllegalArgumentException("Période non reconnue : " + period);
        };
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
