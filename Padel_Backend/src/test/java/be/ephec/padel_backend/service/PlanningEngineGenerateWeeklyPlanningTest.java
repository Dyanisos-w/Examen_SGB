package be.ephec.padel_backend.service;

import be.ephec.padel_backend.DTO.PlanningSlotDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
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
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanningEngineGenerateWeeklyPlanningTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private SiteOpeningHoursRepository siteOpeningHoursRepository;
    @Mock
    private TerrainRepository terrainRepository;
    @Mock
    private SiteClosureService siteClosureService;

    @InjectMocks
    private PlanningEngine planningEngine;

    @Test
    void shouldGenerateWeekPlanningWithAllSlotsAvailable_whenNoClosureAndNoReservation() {

        // Arrange
        Utilisateur user = new Utilisateur("G00001", "Global", "User");

        Site site = new Site();
        site.setSiteId(1);

        Terrain t1 = new Terrain(1, "T1", site);
        Terrain t2 = new Terrain(2, "T2", site);
        site.setTerrains(List.of(t1, t2));

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        when(utilisateurRepository.findById("G00001")).thenReturn(Optional.of(user));
        when(terrainRepository.findBySiteSiteId(1)).thenReturn(List.of(t1, t2));
        when(siteOpeningHoursRepository.findBySiteSiteId(1)).thenReturn(List.of());
        when(reservationRepository.findOccupiedSlotsForWeek(eq(1), any(), any())).thenReturn(List.of());
        when(siteClosureService.isSiteClosedOnDate(any(Site.class), any(LocalDate.class))).thenReturn(false);

        // Act
        List<PlanningSlotDto> planning = planningEngine.generateWeeklyPlanning("G00001", 1, monday);

        // Assert
        assertFalse(planning.isEmpty());
        assertTrue(planning.stream().allMatch(PlanningSlotDto::isDisponible));
    }

    @Test
    void shouldExcludeClosedDayFromPlanning() {

        Utilisateur user = new Utilisateur("G00001", "Global", "User");

        Site site = new Site();
        site.setSiteId(1);

        Terrain t1 = new Terrain(1, "T1", site);
        Terrain t2 = new Terrain(2, "T2", site);
        site.setTerrains(List.of(t1, t2));

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate closedDay = monday.plusDays(3);

        when(utilisateurRepository.findById("G00001")).thenReturn(Optional.of(user));
        when(terrainRepository.findBySiteSiteId(1)).thenReturn(List.of(t1, t2));
        when(siteOpeningHoursRepository.findBySiteSiteId(1)).thenReturn(List.of());
        when(reservationRepository.findOccupiedSlotsForWeek(eq(1), any(), any())).thenReturn(List.of());

        when(siteClosureService.isSiteClosedOnDate(any(Site.class), any(LocalDate.class)))
                .thenAnswer(invocation -> closedDay.equals(invocation.getArgument(1)));

        List<PlanningSlotDto> planning = planningEngine.generateWeeklyPlanning("G00001", 1, monday);

        // Vérification métier principale
        assertTrue(planning.stream().noneMatch(slot -> slot.getDate().equals(closedDay)));

        // Les autres restent disponibles
        assertTrue(planning.stream().allMatch(PlanningSlotDto::isDisponible));
    }
}
