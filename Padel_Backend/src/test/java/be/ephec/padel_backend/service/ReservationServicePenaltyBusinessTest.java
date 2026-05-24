package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.ReservationUtilisateur;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.ReservationUtilisateurRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServicePenaltyBusinessTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationUtilisateurRepository reservationUtilisateurRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Utilisateur organisateur;
    private Utilisateur joueur1;
    private Utilisateur joueur2;
    private Utilisateur joueur3;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        organisateur = new Utilisateur("ORG001", "Org", "User");
        organisateur.setPenaliteMontant(BigDecimal.ZERO);
        organisateur.setInterditReservationJusqua(null);

        joueur1 = new Utilisateur("J001", "J1", "P1");
        joueur2 = new Utilisateur("J002", "J2", "P2");
        joueur3 = new Utilisateur("J003", "J3", "P3");

        reservation = new Reservation();
        reservation.setIdReservation(100);
        reservation.setCreateur(organisateur);
        reservation.setTypeReservation("PRIVATE");
        reservation.setStatut("PRIVATE");
        reservation.setDateReservation(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(18, 0));
    }

    @Test
    void shouldPenalizeOrganizer_whenPrivateMatchBecomesPublicAndRemainsIncompleteBeforeMatchDay() {

        // GIVEN
        // Match privé avec seulement 2 joueurs (organisateur + joueur1), aucun n’a payé
        List<ReservationUtilisateur> participants = List.of(
                new ReservationUtilisateur(reservation, organisateur),
                new ReservationUtilisateur(reservation, joueur1)
        );

        when(reservationRepository.findByDateReservationAndStatutNot(
                eq(LocalDate.now().plusDays(1)), any())
        ).thenReturn(List.of(reservation));

        // Premier appel : nbPlayers avant suppression des impayeurs
        // Deuxième appel : updatedPlayers après suppression
        when(reservationUtilisateurRepository.countByIdReservationId(100))
                .thenReturn(2, 1);

        when(reservationUtilisateurRepository.findByIdReservationId(100))
                .thenReturn(participants);

        // Vérification des préconditions
        assertEquals("PRIVATE", reservation.getTypeReservation());
        assertEquals("PRIVATE", reservation.getStatut());
        assertEquals(organisateur, reservation.getCreateur());
        assertEquals(LocalDate.now().plusDays(1), reservation.getDateReservation());
        assertEquals(BigDecimal.ZERO, organisateur.getPenaliteMontant());
        assertNull(organisateur.getInterditReservationJusqua());

        // WHEN
        reservationService.checkReservationsDayBefore();

        // THEN

        // 1. Le match bascule en public et reste ouvert (1 joueur restant = organisateur)
        assertEquals("PUBLIC", reservation.getTypeReservation());
        assertEquals("OPEN", reservation.getStatut());

        // 2. L’organisateur est pénalisé (7 jours d’interdiction)
        assertNotNull(organisateur.getInterditReservationJusqua());

        // 3. L’organisateur a une dette : 60€ - 0€ collectés des autres joueurs = 60€
        assertTrue(organisateur.getPenaliteMontant().compareTo(BigDecimal.ZERO) > 0);
    }
}

