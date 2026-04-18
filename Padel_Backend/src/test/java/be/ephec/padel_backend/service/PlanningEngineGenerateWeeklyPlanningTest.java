package be.ephec.padel_backend.service;

import be.ephec.padel_backend.DTO.PlanningSlotDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningEngineGenerateWeeklyPlanningTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private SiteOpeningHoursRepository siteOpeningHoursRepository;

    @InjectMocks
    private PlanningEngine planningEngine;

    @Test
    void shouldGenerateWeekPlanningWithSlotsForEachTerrain() {
        Utilisateur user = new Utilisateur("G00001", "Global", "User");

        Terrain t1 = new Terrain(1, "T1", null);
        Terrain t2 = new Terrain(2, "T2", null);
        Site site = new Site();
        site.setSiteId(1);
        site.setTerrains(List.of(t1, t2));

        when(utilisateurRepository.findById("G00001")).thenReturn(Optional.of(user));
        when(siteRepository.findById(1)).thenReturn(Optional.of(site));
        when(siteOpeningHoursRepository.findBySiteSiteId(1)).thenReturn(List.of());

        List<PlanningSlotDto> planning = planningEngine.generateWeeklyPlanning("G00001", 1, LocalDate.now());

        int expected = 7 * 2 * planningEngine.generateSlots().size();
        assertEquals(expected, planning.size());
        assertTrue(planning.stream().noneMatch(PlanningSlotDto::isDisponible));
        assertFalse(planning.isEmpty());
    }
}

