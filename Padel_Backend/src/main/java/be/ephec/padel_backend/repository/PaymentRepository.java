package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Payment;
import be.ephec.padel_backend.model.PaymentStatus;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByReservation(Reservation reservation);

    Payment findByReservationAndUtilisateur(Reservation reservation, Utilisateur utilisateur);

    int countByReservationAndStatutPaiement(Reservation reservation, PaymentStatus statutPaiement);
}
