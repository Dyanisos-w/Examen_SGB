package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.ReservationUtilisateur;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCheckReservationsDayBeforeTest {

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
    void shouldConvertPrivateToPublicAndApplyPenaltyAndDebtDayBefore() {
        Utilisateur organisateur = new Utilisateur("L00040", "Org", "DayBefore");
        Utilisateur joueurImpayeur = new Utilisateur("L00041", "NoPay", "Player");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(500);
        reservation.setCreateur(organisateur);
        reservation.setTypeReservation("PRIVATE");
        reservation.setStatut("PRIVATE");
        reservation.setDateReservation(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(19, 0));

        ReservationUtilisateur ruCreateur = new ReservationUtilisateur();
        ruCreateur.setReservation(reservation);
        ruCreateur.setUtilisateur(organisateur);
        ruCreateur.setMontantDu(15.0);
        ruCreateur.setMontantPaye(0.0);

        ReservationUtilisateur ruImpayeur = new ReservationUtilisateur();
        ruImpayeur.setReservation(reservation);
        ruImpayeur.setUtilisateur(joueurImpayeur);
        ruImpayeur.setMontantDu(15.0);
        ruImpayeur.setMontantPaye(0.0);

        when(reservationRepository.findByDateReservationAndStatutNot(LocalDate.now().plusDays(1), "CANCELLED"))
                .thenReturn(List.of(reservation));
        when(reservationUtilisateurRepository.countByIdReservationId(500)).thenReturn(2, 1);
        when(reservationUtilisateurRepository.findByIdReservationId(500)).thenReturn(List.of(ruCreateur, ruImpayeur));

        reservationService.checkReservationsDayBefore();

        assertTrue("PUBLIC".equalsIgnoreCase(reservation.getTypeReservation()));
        assertTrue("OPEN".equalsIgnoreCase(reservation.getStatut()));
        assertNotNull(organisateur.getInterditReservationJusqua());
        assertTrue(organisateur.getPenaliteMontant().compareTo(BigDecimal.ZERO) > 0);

        verify(reservationUtilisateurRepository).delete(ruImpayeur);
        verify(utilisateurRepository, atLeastOnce()).save(organisateur);
        verify(reservationRepository, atLeastOnce()).save(reservation);
    }
}

