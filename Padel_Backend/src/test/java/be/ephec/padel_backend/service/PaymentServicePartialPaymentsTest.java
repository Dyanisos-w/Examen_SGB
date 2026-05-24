package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Payment;
import be.ephec.padel_backend.model.PaymentStatus;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.PaymentRepository;
import be.ephec.padel_backend.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServicePartialPaymentsTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldNotMaintainReservationWhenOnlyThreePayments() {
        Reservation reservation = new Reservation();
        reservation.setEstMaintenu(false);

        Utilisateur user = new Utilisateur("L00101", "Pay", "Three");
        Payment payment = new Payment(user, BigDecimal.valueOf(15), reservation);

        when(paymentRepository.findById(11)).thenReturn(Optional.of(payment));
        when(paymentRepository.countByReservationAndStatutPaiement(reservation, PaymentStatus.PAYE)).thenReturn(3);

        paymentService.payer(11);

        assertNotEquals(Boolean.TRUE, reservation.getEstMaintenu());
        assertSame(payment.getStatutPaiement(), PaymentStatus.PAYE);
        verify(reservationRepository, never()).save(reservation);
    }
}

