package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Reservation;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCreateReservationTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationUtilisateurRepository reservationUtilisateurRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private TerrainRepository terrainRepository;
    @Mock
    private be.ephec.padel_backend.service.admin.SiteClosureService siteClosureService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldCreatePublicReservationSuccessfully() {
        Utilisateur organisateur = new Utilisateur("L00001", "Dupont", "Alice");
        Site site = new Site();
        site.setSiteId(1);
        Terrain terrain = new Terrain(1, "Terrain A", site);

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime heureDebut = LocalTime.of(10, 0);

        when(utilisateurRepository.findById("L00001")).thenReturn(Optional.of(organisateur));
        when(terrainRepository.findById(1)).thenReturn(Optional.of(terrain));
        when(reservationRepository.findAll()).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(siteClosureService.isSiteClosedOnDate(site, date)).thenReturn(false);

        Reservation created = reservationService.createReservation(
                "L00001", 1, 1, date, heureDebut, "PUBLIC"
        );

        assertNotNull(created);
        assertEquals("PUBLIC", created.getTypeReservation());
        assertEquals("OPEN", created.getStatut());
        assertEquals(heureDebut.plusMinutes(90), created.getHeureFin());
        assertTrue(created.getMontantTotal() > 0);

        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationUtilisateurRepository).save(any());
    }

    @Test
    void shouldRejectReservationWhenTerrainAlreadyBooked() {
        Utilisateur organisateur = new Utilisateur("L00002", "Dupont", "Bob");
        Site site = new Site();
        site.setSiteId(1);
        Terrain terrain = new Terrain(1, "Terrain A", site);

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime heureDebut = LocalTime.of(10, 0);

        // Une réservation active existe déjà sur ce terrain/date/heure
        Reservation existing = new Reservation();
        existing.setTerrain(terrain);
        existing.setDateReservation(date);
        existing.setHeureDebut(heureDebut);
        existing.setStatut("OPEN");

        when(utilisateurRepository.findById("L00002")).thenReturn(Optional.of(organisateur));
        when(terrainRepository.findById(1)).thenReturn(Optional.of(terrain));
        when(reservationRepository.findAll()).thenReturn(List.of(existing));
        when(siteClosureService.isSiteClosedOnDate(site, date)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation("L00002", 1, 1, date, heureDebut, "PUBLIC")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("créne") || ex.getMessage().toLowerCase().contains("reserv"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
