package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.ReservationUtilisateurRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceLeaveReservationTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationUtilisateurRepository reservationUtilisateurRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private TerrainRepository terrainRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldRejectWhenOrganizerTriesToLeaveOwnReservation() {
        Utilisateur organisateur = new Utilisateur("L00020", "Org", "Owner");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(300);
        reservation.setCreateur(organisateur);
        reservation.setTypeReservation("PUBLIC");
        reservation.setStatut("OPEN");
        reservation.setDateReservation(LocalDate.now().plusDays(2));
        reservation.setHeureDebut(LocalTime.of(9, 0));

        when(reservationRepository.findById(300)).thenReturn(Optional.of(reservation));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.leaveReservation(300, "L00020"));

        assertTrue(ex.getMessage().contains("organisateur"));
        verify(reservationUtilisateurRepository, never())
                .deleteByIdReservationIdAndIdUtilisateurMatricule(300, "L00020");
    }
}

