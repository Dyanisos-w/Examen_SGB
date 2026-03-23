package be.ephec.padel_backend.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class PlanningSlotDto {
    private LocalDate date;
    private LocalTime heure;
    private boolean disponible;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setSiteId(Integer siteId) {
    }

    public void setTerrainId(Integer terrainId) {
    }
}
