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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServicePayerEdgeCasesTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.payer(999));
        verify(paymentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldKeepPaidStatusWhenPaymentAlreadyPaid() {
        Reservation reservation = new Reservation();
        Utilisateur user = new Utilisateur("L00100", "Paid", "Already");

        Payment payment = new Payment(user, BigDecimal.valueOf(15), reservation);
        payment.setStatutPaiement(PaymentStatus.PAYE);

        when(paymentRepository.findById(10)).thenReturn(Optional.of(payment));
        when(paymentRepository.countByReservationAndStatutPaiement(reservation, PaymentStatus.PAYE)).thenReturn(2);

        Payment result = paymentService.payer(10);

        assertEquals(PaymentStatus.PAYE, result.getStatutPaiement());
        assertTrue(result.getDatePaiement() != null);
        verify(paymentRepository).save(payment);
        verify(reservationRepository, never()).save(reservation);
    }
}

