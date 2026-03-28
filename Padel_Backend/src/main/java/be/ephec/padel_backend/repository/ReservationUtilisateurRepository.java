package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.ReservationUtilisateur;
import be.ephec.padel_backend.model.ReservationUtilisateurId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationUtilisateurRepository
        extends JpaRepository<ReservationUtilisateur, ReservationUtilisateurId> {

    @Query("SELECT COUNT(ru) FROM ReservationUtilisateur ru WHERE ru.reservation.idReservation = :reservationId")
    int countByIdReservationId(@Param("reservationId") Integer reservationId);

    @Query("SELECT CASE WHEN COUNT(ru) > 0 THEN true ELSE false END FROM ReservationUtilisateur ru " +
           "WHERE ru.reservation.idReservation = :reservationId " +
           "AND ru.utilisateur.matricule = :matricule")
    boolean existsByIdReservationIdAndIdUtilisateurMatricule(@Param("reservationId") Integer reservationId,
                                                             @Param("matricule") String matricule);

    @Modifying
    @Query("DELETE FROM ReservationUtilisateur ru WHERE ru.reservation.idReservation = :reservationId " +
           "AND ru.utilisateur.matricule = :matricule")
    void deleteByIdReservationIdAndIdUtilisateurMatricule(@Param("reservationId") Integer reservationId,
                                                          @Param("matricule") String matricule);

    @Modifying
    @Query("DELETE FROM ReservationUtilisateur ru WHERE ru.reservation.idReservation = :reservationId")
    void deleteAllByIdReservationId(@Param("reservationId") Integer reservationId);

    @Query("SELECT ru FROM ReservationUtilisateur ru WHERE ru.reservation.idReservation = :reservationId")
    List<ReservationUtilisateur> findByIdReservationId(@Param("reservationId") Integer reservationId);
}