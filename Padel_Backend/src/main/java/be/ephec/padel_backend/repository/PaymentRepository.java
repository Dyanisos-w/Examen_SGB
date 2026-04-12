package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Payment;
import be.ephec.padel_backend.model.PaymentStatus;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT COALESCE(SUM(cast(p.montant as double)), 0.0) FROM Payment p WHERE p.datePaiement BETWEEN :start AND :end")
    double sumBetween(@Param("start") LocalDate start,
                      @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(cast(p.montant as double)), 0.0) FROM Payment p WHERE p.datePaiement BETWEEN :start AND :end AND p.reservation.terrain.site.siteId = :siteId")
    double sumBetween(@Param("start") LocalDate start,
                      @Param("end") LocalDate end,
                      @Param("siteId") Integer siteId);

    List<Payment> findByReservation(Reservation reservation);

    Payment findByReservationAndUtilisateur(Reservation reservation, Utilisateur utilisateur);

    int countByReservationAndStatutPaiement(Reservation reservation, PaymentStatus statutPaiement);
}
