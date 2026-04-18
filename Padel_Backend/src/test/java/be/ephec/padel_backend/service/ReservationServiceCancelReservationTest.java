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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCancelReservationTest {

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
    void shouldCancelReservationWhenCalledByOrganizer() {
        Utilisateur organisateur = new Utilisateur("L00030", "Org", "Cancel");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(400);
        reservation.setCreateur(organisateur);
        reservation.setStatut("OPEN");

        when(reservationRepository.findById(400)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(400, "L00030");

        assertEquals("CANCELLED", reservation.getStatut());
        assertTrue("CANCELLED".equalsIgnoreCase(reservation.getStatut()));
        verify(reservationRepository).save(reservation);
        verify(reservationUtilisateurRepository).deleteAllByIdReservationId(400);
    }
}

