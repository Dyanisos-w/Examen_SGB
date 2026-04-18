package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.ReservationUtilisateur;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceListingTest {

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
    void shouldReturnOnlyFutureNonCancelledPublicReservationsForSite() {
        Site site1 = new Site();
        site1.setSiteId(1);
        Site site2 = new Site();
        site2.setSiteId(2);

        Terrain t1 = new Terrain(1, "T1", site1);
        Terrain t2 = new Terrain(2, "T2", site2);

        Reservation keep = new Reservation();
        keep.setTerrain(t1);
        keep.setTypeReservation("PUBLIC");
        keep.setStatut("OPEN");
        keep.setDateReservation(LocalDate.now().plusDays(1));
        keep.setHeureDebut(LocalTime.of(10, 0));

        Reservation cancelled = new Reservation();
        cancelled.setTerrain(t1);
        cancelled.setTypeReservation("PUBLIC");
        cancelled.setStatut("CANCELLED");
        cancelled.setDateReservation(LocalDate.now().plusDays(1));
        cancelled.setHeureDebut(LocalTime.of(11, 0));

        Reservation wrongSite = new Reservation();
        wrongSite.setTerrain(t2);
        wrongSite.setTypeReservation("PUBLIC");
        wrongSite.setStatut("OPEN");
        wrongSite.setDateReservation(LocalDate.now().plusDays(1));
        wrongSite.setHeureDebut(LocalTime.of(12, 0));

        when(reservationRepository.findAll()).thenReturn(List.of(keep, cancelled, wrongSite));

        List<Reservation> result = reservationService.getPublicReservations(1);

        assertEquals(1, result.size());
        assertTrue(result.contains(keep));
        assertFalse(result.contains(cancelled));
    }

    @Test
    void shouldReturnUserReservationsFromParticipationTable() {
        Utilisateur user = new Utilisateur("L00999", "Me", "User");

        Reservation keep = new Reservation();
        keep.setDateReservation(LocalDate.now().plusDays(2));
        keep.setHeureDebut(LocalTime.of(9, 0));
        keep.setStatut("OPEN");

        Reservation old = new Reservation();
        old.setDateReservation(LocalDate.now().minusDays(1));
        old.setHeureDebut(LocalTime.of(9, 0));
        old.setStatut("OPEN");

        ReservationUtilisateur ru1 = new ReservationUtilisateur();
        ru1.setUtilisateur(user);
        ru1.setReservation(keep);

        ReservationUtilisateur ru2 = new ReservationUtilisateur();
        ru2.setUtilisateur(user);
        ru2.setReservation(old);

        when(reservationUtilisateurRepository.findAll()).thenReturn(List.of(ru1, ru2));

        List<Reservation> result = reservationService.getUserReservations("L00999");

        assertEquals(1, result.size());
        assertTrue(result.contains(keep));
        assertFalse(result.contains(old));
    }
}

