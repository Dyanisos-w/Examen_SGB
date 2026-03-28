package be.ephec.padel_backend.service;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.ReservationUtilisateur;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.ReservationUtilisateurRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private static final int MAX_PLAYERS = 4;
    private static final double MATCH_PRICE = 60.0;
    private static final double PLAYER_SHARE = 15.0;
    private static final int PENALTY_DAYS = 7;

    private final ReservationRepository reservationRepository;
    private final ReservationUtilisateurRepository reservationUtilisateurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TerrainRepository terrainRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationUtilisateurRepository reservationUtilisateurRepository,
                              UtilisateurRepository utilisateurRepository,
                              TerrainRepository terrainRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationUtilisateurRepository = reservationUtilisateurRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.terrainRepository = terrainRepository;
    }

    public Reservation createReservation(String userId,
                                         Integer terrainId,
                                         Integer siteId, LocalDate date,
                                         LocalTime heureDebut,
                                         String typeReservation) {

        Utilisateur organisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Terrain terrain = terrainRepository.findById(terrainId)
                .orElseThrow(() -> new RuntimeException("Terrain introuvable"));

        validateOrganizerCanCreate(organisateur, date);
        validateSiteAccess(organisateur, terrain);

        if (!isTerrainSlotAvailable(terrainId, date, heureDebut)) {
            throw new RuntimeException("Créneau déjà réservé ou non autorisé");
        }

        String normalizedType = normalizeType(typeReservation);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(organisateur);
        reservation.setTerrain(terrain);
        reservation.setDateReservation(date);
        reservation.setHeureDebut(heureDebut);
        reservation.setHeureFin(heureDebut.plusMinutes(90));
        reservation.setTypeReservation(normalizedType);
        reservation.setStatut(initialStatus(normalizedType));
        reservation.setCreateur(organisateur);
        reservation.setMontantTotal(MATCH_PRICE);

        Reservation savedReservation = reservationRepository.save(reservation);

        ReservationUtilisateur organisateurLink = new ReservationUtilisateur();
        organisateurLink.setReservation(savedReservation);
        organisateurLink.setUtilisateur(organisateur);
        organisateurLink.setMontantDu(PLAYER_SHARE);
        organisateurLink.setMontantPaye(0.0);
        organisateurLink.setStatutPaiement("PENDING");

        reservationUtilisateurRepository.save(organisateurLink);

        return savedReservation;
    }

    public void joinPublicReservation(Integer reservationId, String userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        validateReservationExistsAndNotStarted(reservation);
        validateReservationIsPublicAndJoinable(reservation);

        if (reservationUtilisateurRepository.existsByIdReservationIdAndIdUtilisateurMatricule(reservationId, userId)) {
            throw new RuntimeException("Utilisateur déjà inscrit à cette réservation");
        }

        int nbPlayers = reservationUtilisateurRepository.countByIdReservationId(reservationId);
        if (nbPlayers >= MAX_PLAYERS) {
            throw new RuntimeException("Réservation complète");
        }

        ReservationUtilisateur ru = new ReservationUtilisateur();
        ru.setReservation(reservation);
        ru.setUtilisateur(utilisateur);
        ru.setMontantDu(PLAYER_SHARE);
        ru.setMontantPaye(0.0);
        ru.setStatutPaiement("PENDING");

        reservationUtilisateurRepository.save(ru);

        int newCount = nbPlayers + 1;
        if (newCount == MAX_PLAYERS) {
            reservation.setStatut("FULL");
        } else {
            reservation.setStatut("OPEN");
        }

        reservationRepository.save(reservation);
    }

    public void addPlayerToPrivateReservation(Integer reservationId,
                                              String organisateurId,
                                              String joueurId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        Utilisateur organisateur = utilisateurRepository.findById(organisateurId)
                .orElseThrow(() -> new RuntimeException("Organisateur introuvable"));

        Utilisateur joueur = utilisateurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable"));

        validateReservationExistsAndNotStarted(reservation);

        if (!"PRIVATE".equalsIgnoreCase(reservation.getTypeReservation())) {
            throw new RuntimeException("Seules les réservations privées permettent l'ajout manuel");
        }

        if (!reservation.getCreateur().getMatricule().equals(organisateur.getMatricule())) {
            throw new RuntimeException("Seul l'organisateur peut ajouter un joueur à une réservation privée");
        }

        if (reservationUtilisateurRepository.existsByIdReservationIdAndIdUtilisateurMatricule(reservationId, joueurId)) {
            throw new RuntimeException("Ce joueur est déjà inscrit");
        }

        int nbPlayers = reservationUtilisateurRepository.countByIdReservationId(reservationId);
        if (nbPlayers >= MAX_PLAYERS) {
            throw new RuntimeException("Réservation complète");
        }

        ReservationUtilisateur ru = new ReservationUtilisateur();
        ru.setReservation(reservation);
        ru.setUtilisateur(joueur);
        ru.setMontantDu(PLAYER_SHARE);
        ru.setMontantPaye(0.0);
        ru.setStatutPaiement("PENDING");

        reservationUtilisateurRepository.save(ru);

        if (nbPlayers + 1 == MAX_PLAYERS) {
            reservation.setStatut("FULL");
        } else {
            reservation.setStatut("PRIVATE");
        }

        reservationRepository.save(reservation);
    }

    public void addPlayerToPrivate(Integer reservationId,
                                   String organisateurId,
                                   String joueurId) {
        addPlayerToPrivateReservation(reservationId, organisateurId, joueurId);
    }

    public void leaveReservation(Integer reservationId, String userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        validateReservationExistsAndNotStarted(reservation);

        if (reservation.getCreateur() != null
                && reservation.getCreateur().getMatricule().equalsIgnoreCase(userId)) {
            throw new RuntimeException("L'organisateur ne peut pas quitter sa propre réservation");
        }

        boolean exists = reservationUtilisateurRepository.existsByIdReservationIdAndIdUtilisateurMatricule(reservationId, userId);
        if (!exists) {
            throw new RuntimeException("L'utilisateur n'est pas inscrit à cette réservation");
        }

        reservationUtilisateurRepository.deleteByIdReservationIdAndIdUtilisateurMatricule(reservationId, userId);

        int nbPlayers = reservationUtilisateurRepository.countByIdReservationId(reservationId);

        if (nbPlayers == 0) {
            reservation.setStatut("CANCELLED");
            reservationRepository.save(reservation);
            return;
        }

        if (nbPlayers < MAX_PLAYERS && !"PRIVATE".equalsIgnoreCase(reservation.getTypeReservation())) {
            reservation.setStatut("OPEN");
            reservationRepository.save(reservation);
        }
    }

    public void cancelReservation(Integer reservationId, String organisateurId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        if (!reservation.getCreateur().getMatricule().equals(organisateurId)) {
            throw new RuntimeException("Seul l'organisateur peut annuler cette réservation");
        }

        reservation.setStatut("CANCELLED");
        reservationRepository.save(reservation);

        reservationUtilisateurRepository.deleteAllByIdReservationId(reservationId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getPublicReservations(Integer siteId) {
        LocalDate today = LocalDate.now();

        return reservationRepository.findAll().stream()
                .filter(r -> r.getTerrain() != null
                        && r.getTerrain().getSite() != null
                        && r.getTerrain().getSite().getSiteId().equals(siteId))
                .filter(r -> "PUBLIC".equalsIgnoreCase(r.getTypeReservation()))
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatut()))
                .filter(r -> r.getDateReservation() != null && !r.getDateReservation().isBefore(today))
                .sorted(Comparator.comparing(Reservation::getDateReservation)
                        .thenComparing(Reservation::getHeureDebut))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(String userId) {
        LocalDate today = LocalDate.now();

        return reservationUtilisateurRepository.findAll().stream()
                .filter(ru -> ru.getUtilisateur() != null
                        && ru.getUtilisateur().getMatricule().equalsIgnoreCase(userId))
                .map(ReservationUtilisateur::getReservation)
                .filter(r -> r != null && r.getDateReservation() != null && !r.getDateReservation().isBefore(today))
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatut()))
                .distinct()
                .sorted(Comparator.comparing(Reservation::getDateReservation)
                        .thenComparing(Reservation::getHeureDebut))
                .toList();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void checkReservationsDayBefore() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Reservation> reservations = reservationRepository.findByDateReservationAndStatutNot(tomorrow, "CANCELLED");

        for (Reservation reservation : reservations) {
            int nbPlayers = reservationUtilisateurRepository.countByIdReservationId(reservation.getIdReservation());

            if ("PRIVATE".equalsIgnoreCase(reservation.getTypeReservation()) && nbPlayers < MAX_PLAYERS) {
                reservation.setTypeReservation("PUBLIC");
                reservation.setStatut("OPEN");

                applyPenaltyToOrganizer(reservation.getCreateur());
                reservationRepository.save(reservation);
            }

            List<ReservationUtilisateur> participants =
                    reservationUtilisateurRepository.findByIdReservationId(reservation.getIdReservation());

            for (ReservationUtilisateur participant : participants) {
                if (!hasPaid(participant)) {
                    boolean isCreator = participant.getUtilisateur().getMatricule()
                            .equals(reservation.getCreateur().getMatricule());

                    if (!isCreator) {
                        reservationUtilisateurRepository.delete(participant);
                    }
                }
            }

            int updatedPlayers = reservationUtilisateurRepository.countByIdReservationId(reservation.getIdReservation());

            if (updatedPlayers == 0) {
                reservation.setStatut("CANCELLED");
                reservationRepository.save(reservation);
                continue;
            }

            if (updatedPlayers >= MAX_PLAYERS) {
                reservation.setStatut("FULL");
            } else {
                reservation.setStatut("OPEN");
            }

            if ("PUBLIC".equalsIgnoreCase(reservation.getTypeReservation()) && updatedPlayers < MAX_PLAYERS) {
                double remaining = MATCH_PRICE - (updatedPlayers * PLAYER_SHARE);
                addDebtToOrganizer(reservation.getCreateur(), remaining);
            }

            reservationRepository.save(reservation);
        }
    }

    private void validateOrganizerCanCreate(Utilisateur organisateur, LocalDate date) {
        if (organisateur.getPenaliteMontant() != null
                && organisateur.getPenaliteMontant().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Réservation impossible : solde dû existant");
        }

        if (organisateur.getInterditReservationJusqua() != null
                && LocalDate.now().isBefore(organisateur.getInterditReservationJusqua())) {
            throw new RuntimeException("Réservation impossible : utilisateur pénalisé");
        }

        int advanceDays = getAdvanceDays(organisateur);
        LocalDate maxReservableDate = LocalDate.now().plusDays(advanceDays);

        if (date.isAfter(maxReservableDate)) {
            throw new RuntimeException("Date non autorisée pour ce type d'abonnement");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Date de réservation dans le passé");
        }
    }

    private void validateReservationExistsAndNotStarted(Reservation reservation) {
        LocalDateTime startDateTime = LocalDateTime.of(
                reservation.getDateReservation(),
                reservation.getHeureDebut()
        );

        if (!LocalDateTime.now().isBefore(startDateTime)) {
            throw new RuntimeException("La réservation a déjà commencé");
        }

        if ("CANCELLED".equalsIgnoreCase(reservation.getStatut())) {
            throw new RuntimeException("Réservation annulée");
        }
    }

    private void validateReservationIsPublicAndJoinable(Reservation reservation) {
        if (!"PUBLIC".equalsIgnoreCase(reservation.getTypeReservation())) {
            throw new RuntimeException("Cette réservation n'est pas publique");
        }

        if ("FULL".equalsIgnoreCase(reservation.getStatut())) {
            throw new RuntimeException("Réservation complète");
        }

        if ("CANCELLED".equalsIgnoreCase(reservation.getStatut())) {
            throw new RuntimeException("Réservation annulée");
        }
    }

    private void applyPenaltyToOrganizer(Utilisateur organisateur) {
        LocalDate baseDate = LocalDate.now();

        if (organisateur.getInterditReservationJusqua() != null
                && organisateur.getInterditReservationJusqua().isAfter(baseDate)) {
            baseDate = organisateur.getInterditReservationJusqua();
        }

        organisateur.setInterditReservationJusqua(baseDate.plusDays(PENALTY_DAYS));
        utilisateurRepository.save(organisateur);
    }

    private void addDebtToOrganizer(Utilisateur organisateur, double amount) {
        BigDecimal currentDebt = organisateur.getPenaliteMontant() == null
                ? BigDecimal.ZERO
                : organisateur.getPenaliteMontant();

        organisateur.setPenaliteMontant(currentDebt.add(BigDecimal.valueOf(amount)));
        utilisateurRepository.save(organisateur);
    }

    private boolean hasPaid(ReservationUtilisateur participant) {
        return participant.getMontantPaye() != null && participant.getMontantPaye() >= participant.getMontantDu();
    }

    private int getAdvanceDays(Utilisateur utilisateur) {
        String matricule = utilisateur.getMatricule() == null
                ? ""
                : utilisateur.getMatricule().toUpperCase();

        if (matricule.startsWith("G")) return 21;
        if (matricule.startsWith("S")) return 14;
        if (matricule.startsWith("L")) return 5;

        return 0;
    }

    private String normalizeType(String typeReservation) {
        if (typeReservation == null) {
            throw new RuntimeException("Type de réservation obligatoire");
        }

        String normalized = typeReservation.trim().toUpperCase();

        if (!normalized.equals("PUBLIC") && !normalized.equals("PRIVATE")) {
            throw new RuntimeException("Type de réservation invalide");
        }

        return normalized;
    }

    private String initialStatus(String typeReservation) {
        return "PUBLIC".equalsIgnoreCase(typeReservation) ? "OPEN" : "PRIVATE";
    }

    private void validateSiteAccess(Utilisateur utilisateur, Terrain terrain) {
        String matricule = utilisateur.getMatricule() == null ? "" : utilisateur.getMatricule().toUpperCase();

        if (!matricule.startsWith("S")) {
            return;
        }

        if (utilisateur.getSiteAssociated() == null
                || terrain.getSite() == null
                || !utilisateur.getSiteAssociated().getSiteId().equals(terrain.getSite().getSiteId())) {
            throw new RuntimeException("Utilisateur SITE non autorisé sur ce site");
        }
    }

    private boolean isTerrainSlotAvailable(Integer terrainId, LocalDate date, LocalTime heureDebut) {
        return reservationRepository.findAll().stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatut()))
                .noneMatch(r -> r.getTerrain() != null
                        && r.getTerrain().getTerrainId().equals(terrainId)
                        && r.getDateReservation().equals(date)
                        && r.getHeureDebut().equals(heureDebut));
    }
}
