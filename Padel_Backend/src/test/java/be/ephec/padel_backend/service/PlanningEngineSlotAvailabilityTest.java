package be.ephec.padel_backend.service;

import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import be.ephec.padel_backend.service.admin.SiteClosureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningEngineSlotAvailabilityTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private TerrainRepository terrainRepository;
    @Mock
    private SiteOpeningHoursRepository siteOpeningHoursRepository;
    @Mock
    private SiteClosureService siteClosureService;

    @InjectMocks
    private PlanningEngine planningEngine;

    @Test
    void shouldReturnFalseWhenSlotIsBookedAndTrueWhenSlotIsFree() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime heure = LocalTime.of(8, 0);

        when(reservationRepository.existsByTerrainAndDateAndHeure(1, date, heure))
                .thenReturn(true)
                .thenReturn(false);

        assertFalse(planningEngine.isSlotAvailable(1, date, heure));
        assertTrue(planningEngine.isSlotAvailable(1, date, heure));
    }
}

