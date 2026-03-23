package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.terrain.site.siteId = :siteId " +
           "AND r.dateReservation = :matchDate " +
           "AND r.heureDebut = :heureDebut")
    long countBySiteAndDateAndSlot(@Param("siteId") Integer siteId,
                                   @Param("matchDate") LocalDate matchDate,
                                   @Param("heureDebut") LocalTime heureDebut);

    boolean existsByTerrainAndDateAndHeure(Integer terrainId, LocalDate date, LocalTime heure);
}
