package be.ephec.padel_backend.service;

import be.ephec.padel_backend.DTO.PlanningSlotDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.SiteOpeningHours;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanningEngine {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SiteRepository siteRepository;
    private final SiteOpeningHoursRepository siteOpeningHoursRepository;

    private static final int GLOBAL_UTILISATEUR_ADVANCE_DAYS = 21;
    private static final int SITE_UTILISATEUR_ADVANCE_DAYS   = 14;
    private static final int FREE_UTILISATEUR_ADVANCE_DAYS   = 5;
    private static final LocalTime DEFAULT_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CLOSING_TIME = LocalTime.of(22, 0);

    public PlanningEngine(ReservationRepository reservationRepository,
                          UtilisateurRepository utilisateurRepository,
                          SiteRepository siteRepository,
                          SiteOpeningHoursRepository siteOpeningHoursRepository) {
        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.siteRepository = siteRepository;
        this.siteOpeningHoursRepository = siteOpeningHoursRepository;
    }

    public List<PlanningSlotDto> generateWeeklyPlanning(String userId, Integer siteId) {

        Utilisateur user = utilisateurRepository.findById(userId).orElseThrow();
        Site site = siteRepository.findById(siteId).orElseThrow();

        LocalDate today = LocalDate.now();
        List<LocalDate> weekDates = getWeekDates(today);
        Map<DayOfWeek, SiteOpeningHours> openingHoursByDay = getOpeningHoursByDay(siteId);
        LocalDate firstReservableDate = calculateFirstReservableDate(user);

        List<PlanningSlotDto> planning = new ArrayList<>();

        for (LocalDate date : weekDates) {
            List<LocalTime> slots = generateSlots(openingHoursByDay.get(date.getDayOfWeek()));

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