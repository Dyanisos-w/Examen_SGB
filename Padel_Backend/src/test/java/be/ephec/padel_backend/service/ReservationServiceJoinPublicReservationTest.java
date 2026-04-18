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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceJoinPublicReservationTest {

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
    void shouldJoinPublicReservationWhenSlotAvailable() {
        Utilisateur createur = new Utilisateur("L00001", "Createur", "One");
        Utilisateur joueur = new Utilisateur("L00002", "Joueur", "Two");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(100);
        reservation.setCreateur(createur);
        reservation.setTypeReservation("PUBLIC");
        reservation.setStatut("OPEN");
        reservation.setDateReservation(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(14, 0));

        when(reservationRepository.findById(100)).thenReturn(Optional.of(reservation));
        when(utilisateurRepository.findById("L00002")).thenReturn(Optional.of(joueur));
        when(reservationUtilisateurRepository.existsByIdReservationIdAndIdUtilisateurMatricule(100, "L00002")).thenReturn(false);
        when(reservationUtilisateurRepository.countByIdReservationId(100)).thenReturn(2);

        reservationService.joinPublicReservation(100, "L00002");

        assertEquals("OPEN", reservation.getStatut());
        assertTrue("PUBLIC".equals(reservation.getTypeReservation()));
        verify(reservationUtilisateurRepository).save(any());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void shouldRejectJoinWhenReservationFull() {
        Utilisateur createur = new Utilisateur("L00003", "Full", "Org");
        Utilisateur joueur   = new Utilisateur("L00004", "New",  "Player");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(101);
        reservation.setCreateur(createur);
        reservation.setTypeReservation("PUBLIC");
        reservation.setStatut("FULL");
        reservation.setDateReservation(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(16, 0));

        when(reservationRepository.findById(101)).thenReturn(Optional.of(reservation));
        when(utilisateurRepository.findById("L00004")).thenReturn(Optional.of(joueur));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.joinPublicReservation(101, "L00004"));

        assertTrue(ex.getMessage().toLowerCase().contains("compl"));
        verify(reservationUtilisateurRepository, never()).save(any());
    }
}
