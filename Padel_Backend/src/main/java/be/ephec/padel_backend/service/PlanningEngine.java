package be.ephec.padel_backend.service;

import be.ephec.padel_backend.DTO.PlanningSlotDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanningEngine {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SiteRepository siteRepository;

    private static final int GLOBAL_UTILISATEUR_ADVANCE_DAYS = 21;
    private static final int SITE_UTILISATEUR_ADVANCE_DAYS   = 14;
    private static final int FREE_UTILISATEUR_ADVANCE_DAYS   = 5;

    public PlanningEngine(ReservationRepository reservationRepository,
                          UtilisateurRepository utilisateurRepository,
                          SiteRepository siteRepository) {
        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.siteRepository = siteRepository;
    }

    public List<PlanningSlotDto> generateWeeklyPlanning(String userId, Integer siteId) {

        Utilisateur user = utilisateurRepository.findById(userId).orElseThrow();
        Site site = siteRepository.findById(siteId).orElseThrow();

        LocalDate today = LocalDate.now();
        List<LocalDate> weekDates = getWeekDates(today);
        List<LocalTime> slots = generateSlots();
        LocalDate firstReservableDate = calculateFirstReservableDate(user);

        List<PlanningSlotDto> planning = new ArrayList<>();

        for (LocalDate date : weekDates) {
            for (Terrain terrain : site.getTerrains()) {
                for (LocalTime heure : slots) {

                    boolean disponible = !date.isBefore(firstReservableDate)
                            && !reservationRepository.existsByTerrainAndDateAndHeure(
                            terrain.getTerrainId(),
                            date,
                            heure
                    );

                    PlanningSlotDto dto = new PlanningSlotDto();
                    dto.setSiteId(siteId);
                    dto.setTerrainId(terrain.getTerrainId());
                    dto.setDate(date);
                    dto.setHeure(heure);
                    dto.setDisponible(disponible);

                    planning.add(dto);
                }
            }
        }

        return planning;
    }

    private List<LocalDate> getWeekDates(LocalDate referenceDate) {

        LocalDate monday = referenceDate.minusDays(
                referenceDate.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()
        );

        List<LocalDate> week = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            week.add(monday.plusDays(i));
        }

        return week;
    }

    public List<LocalTime> generateSlots() {
        List<LocalTime> slots = new ArrayList<>();

        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(22, 0);

        while (!startTime.plusMinutes(90).isAfter(endTime)) {
            slots.add(startTime);
            startTime = startTime.plusMinutes(105);
        }
        return slots;
    }

    private LocalDate calculateFirstReservableDate(Utilisateur utilisateur) {

        LocalDate today = LocalDate.now();

        if (utilisateur.getInterditReservationJusqua() != null &&
                today.isBefore(utilisateur.getInterditReservationJusqua())) {
            today = utilisateur.getInterditReservationJusqua();
        }

        int advanceDays = getAdvanceDays(utilisateur);

        return today.plusDays(advanceDays);
    }

    private int getAdvanceDays(Utilisateur utilisateur) {

        String matricule = utilisateur.getMatricule();

        if (matricule.startsWith("G")) return GLOBAL_UTILISATEUR_ADVANCE_DAYS;
        if (matricule.startsWith("S")) return SITE_UTILISATEUR_ADVANCE_DAYS;
        if (matricule.startsWith("L")) return FREE_UTILISATEUR_ADVANCE_DAYS;

        return 0;
    }
    public boolean isSlotAvailable(Integer terrainId, LocalDate date, LocalTime heure) {
        return !reservationRepository.existsByTerrainAndDateAndHeure(
                terrainId, date, heure
        );
    }
}