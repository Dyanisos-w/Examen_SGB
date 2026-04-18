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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServicePayerTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldSetPaymentPaidAndMaintainReservationWhenFourthPaymentArrives() {
        Utilisateur user = new Utilisateur("L00050", "Pay", "User");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(600);
        reservation.setEstMaintenu(false);

        Payment payment = new Payment(user, BigDecimal.valueOf(15), reservation);

        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(paymentRepository.countByReservationAndStatutPaiement(reservation, PaymentStatus.PAYE)).thenReturn(4);

        Payment returned = paymentService.payer(1);

        assertNotNull(returned);
        assertEquals(PaymentStatus.PAYE, returned.getStatutPaiement());
        assertEquals(LocalDate.now(), returned.getDatePaiement());
        assertTrue(Boolean.TRUE.equals(reservation.getEstMaintenu()));

        verify(paymentRepository).save(payment);
        verify(reservationRepository).save(reservation);
    }
}

