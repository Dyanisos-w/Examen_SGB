package be.ephec.padel_backend.service;

import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.model.ReservationUtilisateur;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.ReservationUtilisateurRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServicePenaltyPersistenceIntegrationTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private TerrainRepository terrainRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationUtilisateurRepository reservationUtilisateurRepository;

    @Test
    void shouldPersistPenaltyDateAndDebtWhenPrivateReservationIsIncompleteDayBefore() {
        Site site = new Site("Site Test", "Rue Test 1");
        site = siteRepository.save(site);

        Terrain terrain = new Terrain(null, "Terrain 1", site);
        terrain = terrainRepository.save(terrain);

        Utilisateur organisateur = new Utilisateur("L90001", "Org", "Test");
        Utilisateur joueurPaye = new Utilisateur("L90002", "Joueur", "Paye");
        Utilisateur joueurImpayeur = new Utilisateur("L90003", "Joueur", "Impayeur");
        utilisateurRepository.save(organisateur);
        utilisateurRepository.save(joueurPaye);
        utilisateurRepository.save(joueurImpayeur);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(organisateur);
        reservation.setCreateur(organisateur);
        reservation.setTerrain(terrain);
        reservation.setDateReservation(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(19, 0));
        reservation.setHeureFin(LocalTime.of(20, 30));
        reservation.setTypeReservation("PRIVATE");
        reservation.setStatut("PRIVATE");
        reservation.setMontantTotal(60.0);
        reservation = reservationRepository.save(reservation);

        ReservationUtilisateur ruOrganisateur = new ReservationUtilisateur();
        ruOrganisateur.setReservation(reservation);
        ruOrganisateur.setUtilisateur(organisateur);
        ruOrganisateur.setMontantDu(15.0);
        ruOrganisateur.setMontantPaye(15.0);
        ruOrganisateur.setStatutPaiement("PAYE");
        reservationUtilisateurRepository.save(ruOrganisateur);

        ReservationUtilisateur ruPaye = new ReservationUtilisateur();
        ruPaye.setReservation(reservation);
        ruPaye.setUtilisateur(joueurPaye);
        ruPaye.setMontantDu(15.0);
        ruPaye.setMontantPaye(15.0);
        ruPaye.setStatutPaiement("PAYE");
        reservationUtilisateurRepository.save(ruPaye);

        ReservationUtilisateur ruImpayeur = new ReservationUtilisateur();
        ruImpayeur.setReservation(reservation);
        ruImpayeur.setUtilisateur(joueurImpayeur);
        ruImpayeur.setMontantDu(15.0);
        ruImpayeur.setMontantPaye(0.0);
        ruImpayeur.setStatutPaiement("PENDING");
        reservationUtilisateurRepository.save(ruImpayeur);

        reservationService.checkReservationsDayBefore();

        Utilisateur organisateurAfter = utilisateurRepository.findById("L90001").orElseThrow();
        Reservation reservationAfter = reservationRepository.findById(reservation.getIdReservation()).orElseThrow();

        assertNotNull(organisateurAfter.getInterditReservationJusqua());
        assertEquals(LocalDate.now().plusDays(7), organisateurAfter.getInterditReservationJusqua());

        // 2 joueurs restants apres suppression de l'impayeur => 60 - (2 * 15) = 30
        assertEquals(0, organisateurAfter.getPenaliteMontant().compareTo(BigDecimal.valueOf(30.0)));

        assertEquals("PUBLIC", reservationAfter.getTypeReservation());
        assertEquals("OPEN", reservationAfter.getStatut());

        assertFalse(reservationUtilisateurRepository
                .existsByIdReservationIdAndIdUtilisateurMatricule(reservation.getIdReservation(), "L90003"));
        assertTrue(reservationUtilisateurRepository
                .existsByIdReservationIdAndIdUtilisateurMatricule(reservation.getIdReservation(), "L90001"));
    }
}

