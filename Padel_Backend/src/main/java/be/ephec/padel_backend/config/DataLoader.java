package be.ephec.padel_backend.config;

import be.ephec.padel_backend.model.*;
import be.ephec.padel_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("docker")
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final SiteRepository siteRepository;
    private final TerrainRepository terrainRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SiteOpeningHoursRepository siteOpeningHoursRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationUtilisateurRepository reservationUtilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (siteRepository.count() > 0) {
            return;
        }

        String pwd = passwordEncoder.encode("Padel@2024");

        // --- Sites ---
        Site bruxelles = siteRepository.save(buildSite("Bruxelles-Centre", "Rue de la Loi 1, 1000 Bruxelles", 3));
        Site liege     = siteRepository.save(buildSite("Liège", "Rue Saint-Gilles 45, 4000 Liège", 2));
        Site gand      = siteRepository.save(buildSite("Gand", "Veldstraat 12, 9000 Gent", 2));

        // --- Terrains ---
        Terrain tA = saveT("Terrain A", bruxelles);
        Terrain tB = saveT("Terrain B", bruxelles);
        saveT("Terrain C", bruxelles);
        Terrain tL1 = saveT("Terrain 1", liege);
        saveT("Terrain 2", liege);
        saveT("Terrain 1", gand);
        saveT("Terrain 2", gand);

        // --- Horaires d'ouverture (lun-sam 08h-22h, dim fermé) ---
        for (Site s : List.of(bruxelles, liege, gand)) {
            for (DayOfWeek d : DayOfWeek.values()) {
                SiteOpeningHours h = new SiteOpeningHours();
                h.setSite(s);
                h.setDayOfWeek(d);
                // Fermé = heures null ; ouvert = heures renseignées
                if (d == DayOfWeek.SATURDAY) {
                    h.setOpeningTime(LocalTime.of(9, 0));
                    h.setClosingTime(LocalTime.of(20, 0));
                } else if (d != DayOfWeek.SUNDAY) {
                    h.setOpeningTime(LocalTime.of(8, 0));
                    h.setClosingTime(LocalTime.of(22, 0));
                }
                siteOpeningHoursRepository.save(h);
            }
        }

        // --- Utilisateurs ---
        // Mot de passe commun : Padel@2024
        Utilisateur ga1  = saveUser("GA00001", "Martin",  "Jean",     pwd, null);
        Utilisateur la1  = saveUser("LA00001", "Dupont",  "Marie",    pwd, bruxelles);
        Utilisateur la2  = saveUser("LA00002", "Lambert", "Pierre",   pwd, liege);
        Utilisateur g1   = saveUser("G00001",  "Lecomte", "Sophie",   pwd, null);
        Utilisateur g2   = saveUser("G00002",  "Renard",  "Lucas",    pwd, null);
        Utilisateur l1   = saveUser("L00001",  "Michel",  "Antoine",  pwd, bruxelles);
        Utilisateur l2   = saveUser("L00002",  "Durand",  "Claire",   pwd, liege);
        Utilisateur s1   = saveUser("S00001",  "Petit",   "Emma",     pwd, null);

        // --- Réservations futures ---
        LocalDate demain      = LocalDate.now().plusDays(1);
        LocalDate apresdemain = LocalDate.now().plusDays(2);

        // R1 : G00001 réserve Terrain A demain 10h-11h30 (PUBLIC, pas encore complet)
        Reservation r1 = buildReservation(g1, tA, demain,
                LocalTime.of(10, 0), LocalTime.of(11, 30),
                "CONFIRME", "PUBLIC", false, g1, 30.0);
        r1 = reservationRepository.save(r1);
        saveRU(r1, g1, "CONFIRME", 15.0, 0.0, "EN_ATTENTE");
        saveRU(r1, l1, "CONFIRME", 15.0, 0.0, "EN_ATTENTE");

        // R2 : L00001 réserve Terrain B demain 14h-15h30 (PUBLIC)
        Reservation r2 = buildReservation(l1, tB, demain,
                LocalTime.of(14, 0), LocalTime.of(15, 30),
                "CONFIRME", "PUBLIC", false, l1, 30.0);
        r2 = reservationRepository.save(r2);
        saveRU(r2, l1, "CONFIRME", 30.0, 30.0, "PAYE");

        // R3 : G00002 réserve Terrain 1 Liège après-demain 09h-10h30 (PRIVATE, complet)
        Reservation r3 = buildReservation(g2, tL1, apresdemain,
                LocalTime.of(9, 0), LocalTime.of(10, 30),
                "CONFIRME", "PRIVATE", true, g2, 60.0);
        r3 = reservationRepository.save(r3);
        saveRU(r3, g2,  "CONFIRME", 15.0, 15.0, "PAYE");
        saveRU(r3, g1,  "CONFIRME", 15.0, 15.0, "PAYE");
        saveRU(r3, l2,  "CONFIRME", 15.0, 15.0, "PAYE");
        saveRU(r3, s1,  "CONFIRME", 15.0, 15.0, "PAYE");
    }

    // --- helpers ---

    private Site buildSite(String nom, String adresse, int nbTerrains) {
        Site s = new Site(nom, adresse);
        s.setNombreTerrains(nbTerrains);
        return s;
    }

    private Terrain saveT(String nom, Site site) {
        Terrain t = new Terrain(null, nom, site);
        return terrainRepository.save(t);
    }

    private Utilisateur saveUser(String matricule, String nom, String prenom, String pwd, Site site) {
        Utilisateur u = new Utilisateur(matricule, nom, prenom);
        u.setPassword(pwd);
        u.setSiteAssociated(site);
        return utilisateurRepository.save(u);
    }

    private Reservation buildReservation(Utilisateur utilisateur, Terrain terrain,
                                         LocalDate date, LocalTime debut, LocalTime fin,
                                         String statut, String type, boolean complet,
                                         Utilisateur createur, double montant) {
        Reservation r = new Reservation();
        r.setUtilisateur(utilisateur);
        r.setTerrain(terrain);
        r.setDateReservation(date);
        r.setHeureDebut(debut);
        r.setHeureFin(fin);
        r.setStatut(statut);
        r.setTypeReservation(type);
        r.setEstComplet(complet);
        r.setEstMaintenu(true);
        r.setCreateur(createur);
        r.setMontantTotal(montant);
        return r;
    }

    private void saveRU(Reservation r, Utilisateur u,
                        String statut, double du, double paye, String statutPaiement) {
        ReservationUtilisateur ru = new ReservationUtilisateur(r, u);
        ru.setStatutResaUser(statut);
        ru.setMontantDu(du);
        ru.setMontantPaye(paye);
        ru.setStatutPaiement(statutPaiement);
        reservationUtilisateurRepository.save(ru);
    }
}
