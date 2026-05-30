package be.ephec.padel_backend.config;

import be.ephec.padel_backend.model.*;
import be.ephec.padel_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Injecte 30 utilisateurs français + un historique complet de réservations
 * du 21 mai au 19 juin 2026 (inclus), couvrant tous les cas métier :
 *   - typeReservation : PUBLIC / PRIVATE
 *   - statut          : CONFIRME / ANNULE
 *   - estComplet      : true / false
 *   - estMaintenu     : true / false
 *   - paiement        : PAYE / EN_ATTENTE / mixte
 *   - utilisateur avec pénalité (penaliteMontant)
 *   - utilisateur interdit de réservation (interditReservationJusqua)
 *
 * Activation : --spring.profiles.active=seed
 * Si les sites n'existent pas encore, les créer automatiquement.
 * Idempotent : skip complet si S00002 existe déjà.
 */
@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class SeedDataLoader implements ApplicationRunner {

    private final SiteRepository                   siteRepository;
    private final TerrainRepository                terrainRepository;
    private final UtilisateurRepository            utilisateurRepository;
    private final ReservationRepository            reservationRepository;
    private final ReservationUtilisateurRepository reservationUtilisateurRepository;
    private final PaymentRepository                paymentRepository;
    private final PasswordEncoder                  passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (utilisateurRepository.existsById("S00002")) {
            log.info("[SeedDataLoader] Données déjà injectées — skip.");
            return;
        }

        // ── Sites ──────────────────────────────────────────────────────────
        List<Site> sites = siteRepository.findAll();
        Site bruxelles, liege, gand;

        if (sites.isEmpty()) {
            bruxelles = siteRepository.save(buildSite("Bruxelles-Centre", "Rue de la Loi 1, 1000 Bruxelles", 3));
            liege     = siteRepository.save(buildSite("Liège",            "Rue Saint-Gilles 45, 4000 Liège",   2));
            gand      = siteRepository.save(buildSite("Gand",             "Veldstraat 12, 9000 Gent",           2));
        } else {
            bruxelles = sites.stream().filter(s -> s.getNom().contains("Brux")).findFirst().orElse(sites.get(0));
            liege     = sites.stream().filter(s -> s.getNom().contains("Li")).findFirst().orElse(sites.get(0));
            gand      = sites.stream().filter(s -> s.getNom().contains("Gand") || s.getNom().contains("Gan"))
                             .findFirst().orElse(sites.get(sites.size() - 1));
        }

        // ── Terrains ───────────────────────────────────────────────────────
        List<Terrain> all = terrainRepository.findAll();
        List<Terrain> tBrux  = all.stream().filter(t -> t.getSite().getSiteId().equals(bruxelles.getSiteId())).toList();
        List<Terrain> tLiege = all.stream().filter(t -> t.getSite().getSiteId().equals(liege.getSiteId())).toList();
        List<Terrain> tGand  = all.stream().filter(t -> t.getSite().getSiteId().equals(gand.getSiteId())).toList();

        if (tBrux.isEmpty())  tBrux  = newTerrains(bruxelles, "Terrain A", "Terrain B", "Terrain C");
        if (tLiege.isEmpty()) tLiege = newTerrains(liege,     "Terrain 1", "Terrain 2");
        if (tGand.isEmpty())  tGand  = newTerrains(gand,      "Terrain 1", "Terrain 2");

        Terrain tBA = tBrux.get(0);
        Terrain tBB = tBrux.size() > 1 ? tBrux.get(1) : tBrux.get(0);
        Terrain tBC = tBrux.size() > 2 ? tBrux.get(2) : tBrux.get(0);
        Terrain tL1 = tLiege.get(0);
        Terrain tL2 = tLiege.size() > 1 ? tLiege.get(1) : tLiege.get(0);
        Terrain tG1 = tGand.get(0);
        Terrain tG2 = tGand.size() > 1 ? tGand.get(1) : tGand.get(0);

        // ── Mot de passe commun ────────────────────────────────────────────
        String pwd = passwordEncoder.encode("password");

        // ==================================================================
        // 30 UTILISATEURS FRANÇAIS
        // S (SiteUser ×10) | L (FreeUser ×10) | G (GlobalUser ×8) | LA (LocalAdmin ×2)
        // Signature : saveUser(matricule, nom, prenom, pwd, site, pénalité, interdit)
        // ==================================================================

        Utilisateur s02  = saveUser("S00002",  "Dubois",    "Thomas",      pwd, bruxelles, null,                  null);
        Utilisateur s03  = saveUser("S00003",  "Leroy",     "Nicolas",     pwd, bruxelles, null,                  null);
        Utilisateur s04  = saveUser("S00004",  "Moreau",    "Pierre",      pwd, bruxelles, BigDecimal.valueOf(15), null); // pénalité impayé
        Utilisateur s05  = saveUser("S00005",  "Simon",     "Marie",       pwd, bruxelles, null,                  null);
        Utilisateur s06  = saveUser("S00006",  "Laurent",   "Sophie",      pwd, liege,     null,                  null);
        Utilisateur s07  = saveUser("S00007",  "Lefebvre",  "Julien",      pwd, liege,     null,                  null);
        Utilisateur s08  = saveUser("S00008",  "Perrin",    "Maxime",      pwd, liege,     null,                  null);
        Utilisateur s09  = saveUser("S00009",  "Bernard",   "Julie",       pwd, gand,      null,                  null);
        Utilisateur s10  = saveUser("S00010",  "Fontaine",  "Baptiste",    pwd, gand,      null,                  null);
        Utilisateur s11  = saveUser("S00011",  "Girard",    "Camille",     pwd, gand,      null,                  null);

        Utilisateur l03  = saveUser("L00003",  "Roux",      "Alexandre",   pwd, bruxelles, null,                  null);
        Utilisateur l04  = saveUser("L00004",  "Vincent",   "Antoine",     pwd, bruxelles, null,                  null);
        Utilisateur l05  = saveUser("L00005",  "Fournier",  "François",    pwd, null,      null,                  null);
        Utilisateur l06  = saveUser("L00006",  "Morel",     "Pauline",     pwd, bruxelles, BigDecimal.valueOf(10), null); // pénalité
        Utilisateur l07  = saveUser("L00007",  "André",     "Chloé",       pwd, liege,     null,                  null);
        Utilisateur l08  = saveUser("L00008",  "Marchand",  "Lucas",       pwd, liege,     null,                  null);
        Utilisateur l09  = saveUser("L00009",  "Lemaire",   "Hugo",        pwd, null,      null,                  null);
        Utilisateur l10  = saveUser("L00010",  "Mercier",   "Gabriel",     pwd, gand,      null,                  null);
        Utilisateur l11  = saveUser("L00011",  "Gauthier",  "Emma",        pwd, gand,      null,                  null);
        Utilisateur l12  = saveUser("L00012",  "Boyer",     "Laura",       pwd, null,      null,                  null);

        Utilisateur g03  = saveUser("G00003",  "Garnier",   "Louis",       pwd, null,      null,                  d(6, 1)); // interdit jusqu'au 1 juin
        Utilisateur g04  = saveUser("G00004",  "Chevalier", "Raphaël",     pwd, null,      null,                  null);
        Utilisateur g05  = saveUser("G00005",  "Fabre",     "Sarah",       pwd, null,      null,                  null);
        Utilisateur g06  = saveUser("G00006",  "Blanchard", "Charlotte",   pwd, null,      null,                  null);
        Utilisateur g07  = saveUser("G00007",  "Bonnet",    "Inès",        pwd, null,      null,                  null);
        Utilisateur g08  = saveUser("G00008",  "François",  "Mathilde",    pwd, null,      null,                  null);
        Utilisateur g09  = saveUser("G00009",  "Guerin",    "Claire",      pwd, null,      null,                  null);
        Utilisateur g10  = saveUser("G00010",  "Meunier",   "Christophe",  pwd, null,      null,                  null);

        Utilisateur la03 = saveUser("LA00003", "Leclerc",   "Manon",       pwd, gand,      null,                  null);
        Utilisateur la04 = saveUser("LA00004", "Rousseau",  "Léa",         pwd, liege,     null,                  null);

        // ==================================================================
        // RÉSERVATIONS 21 mai → 19 juin 2026
        // Colonnes saveRes : createur, terrain, date, debut, fin,
        //                    statut, type, estComplet, estMaintenu, montantTotal
        // ==================================================================

        // ── PASSÉ (21–27 mai) ─────────────────────────────────────────────

        // R01 · PUBLIC · CONFIRME · complet 4/4 · tous PAYE
        var r01 = res(s02, tBA, d(5,21), t(10,0), t(11,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r01,s02,15,15,"PAYE");  ru(r01,s03,15,15,"PAYE");  ru(r01,l03,15,15,"PAYE");  ru(r01,l04,15,15,"PAYE");
        pay(r01,s02,15,d(5,21));   pay(r01,s03,15,d(5,21));   pay(r01,l03,15,d(5,21));   pay(r01,l04,15,d(5,21));

        // R02 · PRIVATE · CONFIRME · complet 4/4 · tous PAYE
        var r02 = res(s06, tL1, d(5,21), t(14,0), t(15,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r02,s06,15,15,"PAYE");  ru(r02,s07,15,15,"PAYE");  ru(r02,l07,15,15,"PAYE");  ru(r02,l08,15,15,"PAYE");
        pay(r02,s06,15,d(5,21));   pay(r02,s07,15,d(5,21));   pay(r02,l07,15,d(5,21));   pay(r02,l08,15,d(5,21));

        // R03 · PUBLIC · CONFIRME · incomplet 2/4 · paiement mixte (1 PAYE / 1 EN_ATTENTE)
        var r03 = res(l03, tBB, d(5,22), t(16,0), t(17,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r03,l03,15,15,"PAYE");  ru(r03,g03,15,0,"EN_ATTENTE");
        pay(r03,l03,15,d(5,22));

        // R04 · PUBLIC · ANNULE · incomplet · rien payé
        var r04 = res(s09, tG1, d(5,22), t(10,0), t(11,30), "ANNULE",   "PUBLIC",  false, false,  0.0);
        ru(r04,s09,15,0,"EN_ATTENTE"); ru(r04,s10,15,0,"EN_ATTENTE");

        // R05 · PRIVATE · CONFIRME · complet · estMaintenu=true · tous PAYE
        var r05 = res(s06, tL2, d(5,23), t(9,0),  t(10,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r05,s06,15,15,"PAYE"); ru(r05,l07,15,15,"PAYE"); ru(r05,g04,15,15,"PAYE"); ru(r05,g05,15,15,"PAYE");
        pay(r05,s06,15,d(5,23)); pay(r05,l07,15,d(5,23)); pay(r05,g04,15,d(5,23)); pay(r05,g05,15,d(5,23));

        // R06 · PUBLIC · CONFIRME · incomplet · s04 n'a pas payé → pénalité sur compte
        var r06 = res(s04, tBC, d(5,23), t(14,0), t(15,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r06,s04,15,0,"EN_ATTENTE"); // impayé → pénalité déjà posée sur l'utilisateur
        ru(r06,s05,15,15,"PAYE");
        pay(r06,s05,15,d(5,23));

        // R07 · PUBLIC · CONFIRME · complet 4/4 · tous PAYE
        var r07 = res(g03, tBA, d(5,25), t(10,0), t(11,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r07,g03,15,15,"PAYE"); ru(r07,g04,15,15,"PAYE"); ru(r07,s02,15,15,"PAYE"); ru(r07,l05,15,15,"PAYE");
        pay(r07,g03,15,d(5,25)); pay(r07,g04,15,d(5,25)); pay(r07,s02,15,d(5,25)); pay(r07,l05,15,d(5,25));

        // R08 · PRIVATE · CONFIRME · complet · paiement partiel (2 PAYE / 2 EN_ATTENTE)
        var r08 = res(l07, tL1, d(5,25), t(16,0), t(17,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r08,l07,15,15,"PAYE"); ru(r08,s06,15,15,"PAYE"); ru(r08,g06,15,0,"EN_ATTENTE"); ru(r08,l09,15,0,"EN_ATTENTE");
        pay(r08,l07,15,d(5,25)); pay(r08,s06,15,d(5,25));

        // R09 · PRIVATE · ANNULE · complet · estMaintenu=false · rien payé
        var r09 = res(la03, tG2, d(5,26), t(11,0), t(12,30), "ANNULE",  "PRIVATE", true,  false,  0.0);
        ru(r09,la03,15,0,"EN_ATTENTE"); ru(r09,s11,15,0,"EN_ATTENTE");
        ru(r09,l10,15,0,"EN_ATTENTE");  ru(r09,l11,15,0,"EN_ATTENTE");

        // R10 · PRIVATE · CONFIRME · complet 4/4 · tous PAYE
        var r10 = res(s09, tG1, d(5,26), t(14,0), t(15,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r10,s09,15,15,"PAYE"); ru(r10,s10,15,15,"PAYE"); ru(r10,l10,15,15,"PAYE"); ru(r10,l11,15,15,"PAYE");
        pay(r10,s09,15,d(5,26)); pay(r10,s10,15,d(5,26)); pay(r10,l10,15,d(5,26)); pay(r10,l11,15,d(5,26));

        // R11 · PUBLIC · CONFIRME · complet · paiement mixte (2 PAYE / 2 EN_ATTENTE)
        var r11 = res(l04, tBB, d(5,27), t(18,0), t(19,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r11,l04,15,15,"PAYE"); ru(r11,s03,15,15,"PAYE"); ru(r11,g07,15,0,"EN_ATTENTE"); ru(r11,g08,15,0,"EN_ATTENTE");
        pay(r11,l04,15,d(5,27)); pay(r11,s03,15,d(5,27));

        // R12 · PUBLIC · CONFIRME · créateur seul (1/4) · PAYE
        var r12 = res(la04, tL2, d(5,27), t(10,0), t(11,30), "CONFIRME", "PUBLIC", false, true,  15.0);
        ru(r12,la04,15,15,"PAYE");
        pay(r12,la04,15,d(5,27));

        // ── AUJOURD'HUI (28 mai) ──────────────────────────────────────────

        // R13 · PUBLIC · CONFIRME · complet · tous EN_ATTENTE
        var r13 = res(g09, tBA, d(5,28), t(12,0), t(13,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r13,g09,15,0,"EN_ATTENTE"); ru(r13,g10,15,0,"EN_ATTENTE");
        ru(r13,l12,15,0,"EN_ATTENTE"); ru(r13,s04,15,0,"EN_ATTENTE");

        // R14 · PRIVATE · CONFIRME · incomplet 2/4 · 1 pré-payé
        var r14 = res(l10, tG2, d(5,28), t(16,0), t(17,30), "CONFIRME", "PRIVATE", false, true,  30.0);
        ru(r14,l10,15,15,"PAYE"); ru(r14,s11,15,0,"EN_ATTENTE");
        pay(r14,l10,15,d(5,28));

        // ── FUTUR (29 mai → 19 juin) ─────────────────────────────────────

        // R15 · 29 mai | PUBLIC · incomplet 2/4 · EN_ATTENTE
        var r15 = res(s02, tBB, d(5,29), t(10,0), t(11,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r15,s02,15,0,"EN_ATTENTE"); ru(r15,l03,15,0,"EN_ATTENTE");

        // R16 · 30 mai | PRIVATE · complet · tous EN_ATTENTE
        var r16 = res(l07, tL1, d(5,30), t(14,0), t(15,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r16,l07,15,0,"EN_ATTENTE"); ru(r16,s06,15,0,"EN_ATTENTE"); ru(r16,s08,15,0,"EN_ATTENTE"); ru(r16,la04,15,0,"EN_ATTENTE");

        // R17 · 31 mai | PUBLIC · 3 joueurs · 1 pré-payé
        var r17 = res(s09, tG1, d(5,31), t(10,0), t(11,30), "CONFIRME", "PUBLIC",  false, true,  45.0);
        ru(r17,s09,15,15,"PAYE"); ru(r17,l10,15,0,"EN_ATTENTE"); ru(r17,l11,15,0,"EN_ATTENTE");
        pay(r17,s09,15,d(5,29));

        // R18 · 1 juin  | PRIVATE · complet · EN_ATTENTE (g03 libéré ce jour)
        var r18 = res(g04, tBC, d(6,1),  t(16,0), t(17,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r18,g04,15,0,"EN_ATTENTE"); ru(r18,g05,15,0,"EN_ATTENTE"); ru(r18,l05,15,0,"EN_ATTENTE"); ru(r18,l09,15,0,"EN_ATTENTE");

        // R19 · 2 juin  | PUBLIC · 2 joueurs · EN_ATTENTE
        var r19 = res(s07, tL2, d(6,2),  t(11,0), t(12,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r19,s07,15,0,"EN_ATTENTE"); ru(r19,g06,15,0,"EN_ATTENTE");

        // R20 · 3 juin  | PRIVATE · complet · EN_ATTENTE
        var r20 = res(la03, tG2, d(6,3), t(14,0), t(15,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r20,la03,15,0,"EN_ATTENTE"); ru(r20,s11,15,0,"EN_ATTENTE"); ru(r20,l10,15,0,"EN_ATTENTE"); ru(r20,l12,15,0,"EN_ATTENTE");

        // R21 · 4 juin  | PUBLIC · complet · 1 pré-payé
        var r21 = res(s05, tBA, d(6,4),  t(10,0), t(11,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r21,s05,15,15,"PAYE"); ru(r21,l06,15,0,"EN_ATTENTE"); ru(r21,g07,15,0,"EN_ATTENTE"); ru(r21,g08,15,0,"EN_ATTENTE");
        pay(r21,s05,15,d(5,28));

        // R22 · 5 juin  | PRIVATE · créateur seul (1/4)
        var r22 = res(l07, tL1, d(6,5),  t(18,0), t(19,30), "CONFIRME", "PRIVATE", false, true,  15.0);
        ru(r22,l07,15,0,"EN_ATTENTE");

        // R23 · 6 juin  | PUBLIC · complet · EN_ATTENTE
        var r23 = res(g09, tBB, d(6,6),  t(10,0), t(11,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r23,g09,15,0,"EN_ATTENTE"); ru(r23,g10,15,0,"EN_ATTENTE"); ru(r23,s02,15,0,"EN_ATTENTE"); ru(r23,s03,15,0,"EN_ATTENTE");

        // R24 · 7 juin  | PRIVATE · 3 joueurs · 1 pré-payé
        var r24 = res(s09, tG1, d(6,7),  t(14,0), t(15,30), "CONFIRME", "PRIVATE", false, true,  45.0);
        ru(r24,s09,15,15,"PAYE"); ru(r24,s10,15,0,"EN_ATTENTE"); ru(r24,la03,15,0,"EN_ATTENTE");
        pay(r24,s09,15,d(5,28));

        // R25 · 8 juin  | PUBLIC · 2 joueurs · EN_ATTENTE
        var r25 = res(l04, tBC, d(6,8),  t(10,0), t(11,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r25,l04,15,0,"EN_ATTENTE"); ru(r25,s04,15,0,"EN_ATTENTE");

        // R26 · 9 juin  | PRIVATE · complet · EN_ATTENTE
        var r26 = res(la04, tL2, d(6,9), t(16,0), t(17,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r26,la04,15,0,"EN_ATTENTE"); ru(r26,s06,15,0,"EN_ATTENTE"); ru(r26,s07,15,0,"EN_ATTENTE"); ru(r26,s08,15,0,"EN_ATTENTE");

        // R27 · 10 juin | PUBLIC · complet · tous pré-payés
        var r27 = res(l11, tG2, d(6,10), t(10,0), t(11,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r27,l11,15,15,"PAYE"); ru(r27,l10,15,15,"PAYE"); ru(r27,s11,15,15,"PAYE"); ru(r27,s10,15,15,"PAYE");
        pay(r27,l11,15,d(5,28)); pay(r27,l10,15,d(5,28)); pay(r27,s11,15,d(5,28)); pay(r27,s10,15,d(5,28));

        // R28 · 11 juin | PUBLIC · 2 joueurs (g03 libéré depuis le 1 juin)
        var r28 = res(g03, tBA, d(6,11), t(11,0), t(12,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r28,g03,15,0,"EN_ATTENTE"); ru(r28,l05,15,0,"EN_ATTENTE");

        // R29 · 12 juin | PRIVATE · complet · EN_ATTENTE
        var r29 = res(s06, tL1, d(6,12), t(14,0), t(15,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r29,s06,15,0,"EN_ATTENTE"); ru(r29,s07,15,0,"EN_ATTENTE"); ru(r29,l07,15,0,"EN_ATTENTE"); ru(r29,l08,15,0,"EN_ATTENTE");

        // R30 · 13 juin | PUBLIC · 3 joueurs · EN_ATTENTE
        var r30 = res(l03, tBB, d(6,13), t(10,0), t(11,30), "CONFIRME", "PUBLIC",  false, true,  45.0);
        ru(r30,l03,15,0,"EN_ATTENTE"); ru(r30,l04,15,0,"EN_ATTENTE"); ru(r30,g05,15,0,"EN_ATTENTE");

        // R31 · 14 juin | PUBLIC · complet · EN_ATTENTE
        var r31 = res(s09, tG1, d(6,14), t(16,0), t(17,30), "CONFIRME", "PUBLIC",  true,  true,  60.0);
        ru(r31,s09,15,0,"EN_ATTENTE"); ru(r31,s10,15,0,"EN_ATTENTE"); ru(r31,l10,15,0,"EN_ATTENTE"); ru(r31,la03,15,0,"EN_ATTENTE");

        // R32 · 15 juin | PRIVATE · 2 joueurs · EN_ATTENTE
        var r32 = res(g09, tBC, d(6,15), t(10,0), t(11,30), "CONFIRME", "PRIVATE", false, true,  30.0);
        ru(r32,g09,15,0,"EN_ATTENTE"); ru(r32,g10,15,0,"EN_ATTENTE");

        // R33 · 16 juin | PUBLIC · complet · 1 pré-payé
        var r33 = res(la04, tL2, d(6,16), t(14,0), t(15,30), "CONFIRME", "PUBLIC", true,  true,  60.0);
        ru(r33,la04,15,15,"PAYE"); ru(r33,s08,15,0,"EN_ATTENTE"); ru(r33,l09,15,0,"EN_ATTENTE"); ru(r33,g06,15,0,"EN_ATTENTE");
        pay(r33,la04,15,d(5,28));

        // R34 · 17 juin | PRIVATE · complet · EN_ATTENTE
        var r34 = res(l10, tG2, d(6,17), t(10,0), t(11,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r34,l10,15,0,"EN_ATTENTE"); ru(r34,l11,15,0,"EN_ATTENTE"); ru(r34,s11,15,0,"EN_ATTENTE"); ru(r34,s09,15,0,"EN_ATTENTE");

        // R35 · 18 juin | PUBLIC · 2 joueurs · EN_ATTENTE
        var r35 = res(s02, tBA, d(6,18), t(11,0), t(12,30), "CONFIRME", "PUBLIC",  false, true,  30.0);
        ru(r35,s02,15,0,"EN_ATTENTE"); ru(r35,s05,15,0,"EN_ATTENTE");

        // R36 · 19 juin | PRIVATE · complet · EN_ATTENTE (dernière date)
        var r36 = res(s06, tL1, d(6,19), t(14,0), t(15,30), "CONFIRME", "PRIVATE", true,  true,  60.0);
        ru(r36,s06,15,0,"EN_ATTENTE"); ru(r36,s07,15,0,"EN_ATTENTE"); ru(r36,g04,15,0,"EN_ATTENTE"); ru(r36,g05,15,0,"EN_ATTENTE");

        log.info("[SeedDataLoader] Injection terminée : 30 utilisateurs, {} réservations au total.",
                 reservationRepository.count());
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Site buildSite(String nom, String adresse, int nbTerrains) {
        Site s = new Site(nom, adresse);
        s.setNombreTerrains(nbTerrains);
        return s;
    }

    private List<Terrain> newTerrains(Site site, String... noms) {
        List<Terrain> result = new ArrayList<>();
        for (String nom : noms) {
            result.add(terrainRepository.save(new Terrain(null, nom, site)));
        }
        return result;
    }

    private Utilisateur saveUser(String matricule, String nom, String prenom, String pwd,
                                 Site site, BigDecimal penalite, LocalDate interdit) {
        Utilisateur u = new Utilisateur(matricule, nom, prenom);
        u.setPassword(pwd);
        u.setSiteAssociated(site);
        if (penalite != null) u.setPenaliteMontant(penalite);
        if (interdit  != null) u.setInterditReservationJusqua(interdit);
        return utilisateurRepository.save(u);
    }

    private Reservation res(Utilisateur createur, Terrain terrain, LocalDate date,
                            LocalTime debut, LocalTime fin, String statut, String type,
                            boolean complet, boolean maintenu, double montant) {
        Reservation r = new Reservation();
        r.setUtilisateur(createur);
        r.setCreateur(createur);
        r.setTerrain(terrain);
        r.setDateReservation(date);
        r.setHeureDebut(debut);
        r.setHeureFin(fin);
        r.setStatut(statut);
        r.setTypeReservation(type);
        r.setEstComplet(complet);
        r.setEstMaintenu(maintenu);
        r.setMontantTotal(montant);
        return reservationRepository.save(r);
    }

    private void ru(Reservation r, Utilisateur u, double du, double paye, String statutPaiement) {
        ReservationUtilisateur ru = new ReservationUtilisateur(r, u);
        ru.setStatutResaUser("CONFIRME");
        ru.setMontantDu(du);
        ru.setMontantPaye(paye);
        ru.setStatutPaiement(statutPaiement);
        reservationUtilisateurRepository.save(ru);
    }

    private void pay(Reservation r, Utilisateur u, double montant, LocalDate date) {
        Payment p = new Payment();
        p.setReservation(r);
        p.setUtilisateur(u);
        p.setMontant(BigDecimal.valueOf(montant));
        p.setDatePaiement(date);
        p.setStatutPaiement(PaymentStatus.PAYE);
        paymentRepository.save(p);
    }

    private static LocalDate d(int month, int day) { return LocalDate.of(2026, month, day); }
    private static LocalTime t(int h, int m)        { return LocalTime.of(h, m); }
}
