package be.ephec.padel_backend.service;

import be.ephec.padel_backend.DTO.PlanningSlotDto;
import be.ephec.padel_backend.model.SiteOpeningHours;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanningEngine {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TerrainRepository terrainRepository;
    private final SiteOpeningHoursRepository siteOpeningHoursRepository;

    private static final int GLOBAL_UTILISATEUR_ADVANCE_DAYS = 21;
    private static final int SITE_UTILISATEUR_ADVANCE_DAYS   = 14;
    private static final int FREE_UTILISATEUR_ADVANCE_DAYS   = 5;
    private static final LocalTime DEFAULT_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CLOSING_TIME = LocalTime.of(22, 0);

    public PlanningEngine(ReservationRepository reservationRepository,
                          UtilisateurRepository utilisateurRepository,
                          TerrainRepository terrainRepository,
                          SiteOpeningHoursRepository siteOpeningHoursRepository) {
        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.terrainRepository = terrainRepository;
        this.siteOpeningHoursRepository = siteOpeningHoursRepository;
    }

    public List<PlanningSlotDto> generateWeeklyPlanning(String userId, Integer siteId, LocalDate referenceDate) {

        // ── 1 query user ─────────────────────────────────────────────────────
        Utilisateur user = utilisateurRepository.findById(userId).orElseThrow();
        LocalDate today = LocalDate.now();
        LocalDate maxReservableDate = today.plusDays(getAdvanceDays(user));
        LocalDate planningReference = referenceDate != null ? referenceDate : LocalDate.now();
        List<LocalDate> weekDates = getWeekDates(planningReference);
        Map<DayOfWeek, SiteOpeningHours> openingHoursByDay = getOpeningHoursByDay(siteId);


        // ── 1 query terrains (remplace le lazy-load site.getTerrains()) ──────
        List<Terrain> terrains = terrainRepository.findBySiteSiteId(siteId);

        // ── 1 query batch réservations (remplace ~189 existsByTerrain…) ──────
        Set<String> occupiedKeys = buildOccupiedSlotSet(
                siteId, weekDates.get(0), weekDates.get(weekDates.size() - 1));

        List<PlanningSlotDto> planning = new ArrayList<>();

        for (LocalDate date : weekDates) {
            List<LocalTime> slots = generateSlots(openingHoursByDay.get(date.getDayOfWeek()));

            for (Terrain terrain : terrains) {
                for (LocalTime heure : slots) {

                    // Vérification en mémoire — 0 requête supplémentaire
                    String key = terrain.getTerrainId() + "_" + date + "_" + heure;
                    boolean disponible = !date.isBefore(today) &&
                            !date.isAfter(maxReservableDate) &&
                            !occupiedKeys.contains(key);

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

    /**
     * Charge en UNE seule requête tous les créneaux réservés pour la semaine,
     * et les indexe sous la forme "terrainId_date_heure" pour lookup O(1).
     */
    private Set<String> buildOccupiedSlotSet(Integer siteId, LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findOccupiedSlotsForWeek(siteId, startDate, endDate)
                .stream()
                .map(row -> row[0] + "_" + row[1] + "_" + row[2])
                .collect(Collectors.toSet());
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
        return generateSlots(null);
    }

    public List<LocalTime> generateSlots(SiteOpeningHours openingHours) {
        List<LocalTime> slots = new ArrayList<>();

        if (openingHours != null && openingHours.isClosed()) {
            return slots;
        }

        LocalTime startTime = openingHours != null && openingHours.getOpeningTime() != null
                ? openingHours.getOpeningTime()
                : DEFAULT_OPENING_TIME;
        LocalTime endTime = openingHours != null && openingHours.getClosingTime() != null
                ? openingHours.getClosingTime()
                : DEFAULT_CLOSING_TIME;

        while (!startTime.plusMinutes(90).isAfter(endTime)) {
            slots.add(startTime);
            startTime = startTime.plusMinutes(105);
        }
        return slots;
    }

    private Map<DayOfWeek, SiteOpeningHours> getOpeningHoursByDay(Integer siteId) {
        Map<DayOfWeek, SiteOpeningHours> openingHoursByDay = new EnumMap<>(DayOfWeek.class);
        for (SiteOpeningHours openingHours : siteOpeningHoursRepository.findBySiteSiteId(siteId)) {
            openingHoursByDay.put(openingHours.getDayOfWeek(), openingHours);
        }
        return openingHoursByDay;
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