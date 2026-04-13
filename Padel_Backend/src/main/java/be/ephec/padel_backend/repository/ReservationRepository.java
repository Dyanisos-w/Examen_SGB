package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.dateReservation BETWEEN :start AND :end")
    long countBetween(@Param("start") LocalDate start,
                      @Param("end") LocalDate end);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.dateReservation BETWEEN :start AND :end AND r.terrain.site.siteId = :siteId")
    long countBetween(@Param("start") LocalDate start,
                      @Param("end") LocalDate end,
                      @Param("siteId") Integer siteId);

    @Query("SELECT COUNT(DISTINCT r.utilisateur.matricule) FROM Reservation r WHERE r.dateReservation BETWEEN :start AND :end")
    int countDistinct(@Param("start") LocalDate start,
                      @Param("end") LocalDate end);

    @Query("SELECT COUNT(DISTINCT r.utilisateur.matricule) FROM Reservation r WHERE r.dateReservation BETWEEN :start AND :end AND r.terrain.site.siteId = :siteId")
    int countDistinct(@Param("start") LocalDate start,
                      @Param("end") LocalDate end,
                      @Param("siteId") Integer siteId);

    @Query("SELECT r FROM Reservation r " +
           "WHERE r.dateReservation BETWEEN :start AND :end " +
           "ORDER BY r.dateReservation DESC, r.heureDebut DESC")
    List<Reservation> findBetween(@Param("start") LocalDate start,
                                  @Param("end") LocalDate end);

    @Query("SELECT r FROM Reservation r " +
           "WHERE r.dateReservation BETWEEN :start AND :end " +
           "AND r.terrain.site.siteId = :siteId " +
           "ORDER BY r.dateReservation DESC, r.heureDebut DESC")
    List<Reservation> findBetween(@Param("start") LocalDate start,
                                  @Param("end") LocalDate end,
                                  @Param("siteId") Integer siteId);

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.dateReservation BETWEEN :start AND :end " +
           "AND UPPER(COALESCE(r.statut, '')) = 'CANCELLED'")
    long countCancelledBetween(@Param("start") LocalDate start,
                               @Param("end") LocalDate end);

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.dateReservation BETWEEN :start AND :end " +
           "AND r.terrain.site.siteId = :siteId " +
           "AND UPPER(COALESCE(r.statut, '')) = 'CANCELLED'")
    long countCancelledBetween(@Param("start") LocalDate start,
                               @Param("end") LocalDate end,
                               @Param("siteId") Integer siteId);

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.terrain.site.siteId = :siteId " +
           "AND r.dateReservation = :matchDate " +
           "AND r.heureDebut = :heureDebut")
    long countBySiteAndDateAndSlot(@Param("siteId") Integer siteId,
                                   @Param("matchDate") LocalDate matchDate,
                                   @Param("heureDebut") LocalTime heureDebut);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
           "WHERE r.terrain.terrainId = :terrainId " +
           "AND r.dateReservation = :date " +
           "AND r.heureDebut = :heure " +
           "AND UPPER(COALESCE(r.statut, '')) <> 'CANCELLED'")
    boolean existsByTerrainAndDateAndHeure(@Param("terrainId") Integer terrainId,
                                           @Param("date") LocalDate date,
                                           @Param("heure") LocalTime heure);

    @Query("SELECT r FROM Reservation r " +
           "WHERE r.terrain.site.siteId = :siteId " +
           "AND UPPER(r.typeReservation) = 'PUBLIC' " +
           "AND UPPER(COALESCE(r.statut, '')) <> 'CANCELLED' " +
           "AND r.dateReservation >= :dateMin " +
           "ORDER BY r.dateReservation, r.heureDebut")
    List<Reservation> findPublicReservationsBySiteId(@Param("siteId") Integer siteId,
                                                     @Param("dateMin") LocalDate dateMin);

    @Query("SELECT r FROM Reservation r " +
           "WHERE r.utilisateur.matricule = :matricule " +
           "AND UPPER(COALESCE(r.statut, '')) <> 'CANCELLED' " +
           "AND r.dateReservation >= :dateMin " +
           "ORDER BY r.dateReservation, r.heureDebut")
    List<Reservation> findReservationsByUtilisateurMatricule(@Param("matricule") String matricule,
                                                             @Param("dateMin") LocalDate dateMin);

    List<Reservation> findByDateReservationAndStatutNot(LocalDate dateReservation, String statut);

    /**
     * Charge en UNE seule requête tous les créneaux occupés pour un site et une plage de dates.
     * Remplace les N×M appels à existsByTerrainAndDateAndHeure dans PlanningEngine.
     * Retourne des Object[3] : [terrainId, dateReservation, heureDebut]
     */
    @Query("SELECT r.terrain.terrainId, r.dateReservation, r.heureDebut " +
           "FROM Reservation r " +
           "WHERE r.terrain.site.siteId = :siteId " +
           "AND r.dateReservation BETWEEN :startDate AND :endDate " +
           "AND UPPER(COALESCE(r.statut, '')) <> 'CANCELLED'")
    List<Object[]> findOccupiedSlotsForWeek(@Param("siteId") Integer siteId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
