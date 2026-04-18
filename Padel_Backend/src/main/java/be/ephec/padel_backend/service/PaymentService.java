package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.*;
import be.ephec.padel_backend.repository.PaymentRepository;
import be.ephec.padel_backend.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    public Payment payer(Integer paymentId) {

        Payment payment = paymentRepository.findById(paymentId).orElseThrow();

        payment.setStatutPaiement(PaymentStatus.PAYE);
        payment.setDatePaiement(LocalDate.now());

        paymentRepository.save(payment);

        verifierPaiementComplet(payment.getReservation());

        return payment;
    }

    private void verifierPaiementComplet(Reservation reservation) {

        int nbPaiements = paymentRepository
                .countByReservationAndStatutPaiement(reservation, PaymentStatus.PAYE);

        if (nbPaiements == 4) {
            reservation.setEstMaintenu(true);
            reservationRepository.save(reservation);
        }
    }

    public List<Payment> getPaymentsByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }
}