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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceAddPlayerToPrivateReservationTest {

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
    void shouldMarkReservationFullWhenFourthPlayerIsAddedToPrivate() {
        Utilisateur organisateur = new Utilisateur("L00010", "Org", "One");
        Utilisateur joueur = new Utilisateur("L00011", "Player", "Two");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(200);
        reservation.setCreateur(organisateur);
        reservation.setTypeReservation("PRIVATE");
        reservation.setStatut("PRIVATE");
        reservation.setDateReservation(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(18, 0));

        when(reservationRepository.findById(200)).thenReturn(Optional.of(reservation));
        when(utilisateurRepository.findById("L00010")).thenReturn(Optional.of(organisateur));
        when(utilisateurRepository.findById("L00011")).thenReturn(Optional.of(joueur));
        when(reservationUtilisateurRepository.existsByIdReservationIdAndIdUtilisateurMatricule(200, "L00011")).thenReturn(false);
        when(reservationUtilisateurRepository.countByIdReservationId(200)).thenReturn(3);

        reservationService.addPlayerToPrivateReservation(200, "L00010", "L00011");

        assertEquals("FULL", reservation.getStatut());
        assertTrue("PRIVATE".equalsIgnoreCase(reservation.getTypeReservation()));
        verify(reservationUtilisateurRepository).save(any());
        verify(reservationRepository).save(reservation);
    }
}

