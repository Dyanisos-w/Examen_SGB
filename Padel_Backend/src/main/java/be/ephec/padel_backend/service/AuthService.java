package be.ephec.padel_backend.service;

import be.ephec.padel_backend.config.JwtUtil;
import be.ephec.padel_backend.config.RoleExtractor;
import be.ephec.padel_backend.controller.AuthController;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final SiteRepository        siteRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;

    /**
     * Authentifie un utilisateur en vérifiant le mot de passe en base de données
     * et génère un JWT contenant matricule, rôle et siteId.
     *
     * @param matricule l'identifiant unique
     * @param password le mot de passe en clair
     * @return un LoginToken contenant accessToken et refreshToken
     * @throws IllegalArgumentException si utilisateur non trouvé ou password invalide
     */
    public AuthController.LoginToken login(String matricule, String password) {
        // Charger l'utilisateur depuis la BD
        Utilisateur utilisateur = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + matricule));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, utilisateur.getPassword())) {
            throw new IllegalArgumentException("Mot de passe incorrect");
        }

        // Extraire le rôle du préfixe matricule
        String role = RoleExtractor.extractRole(matricule);

        // Récupérer le siteId si associé
        Integer siteId = null;
        if (utilisateur.getSiteAssociated() != null) {
            siteId = utilisateur.getSiteAssociated().getSiteId();
        }

        // Générer les tokens JWT
        String accessToken = jwtUtil.generateToken(matricule, role, siteId);
        String refreshToken = jwtUtil.generateRefreshToken(utilisateur.getMatricule());

        return new AuthController.LoginToken(accessToken, refreshToken);
    }

    /**
     * Rafraîchit l'access token en rechargeant l'utilisateur depuis la BD.
     * Permet de mettre à jour role et siteId s'ils ont changé.
     *
     * @param matricule l'identifiant unique de l'utilisateur
     * @return un LoginToken contenant le nouvel accessToken
     * @throws IllegalArgumentException si utilisateur non trouvé
     */
    public String refreshAccessToken(String matricule) {
        // Recharger l'utilisateur depuis la BD
        Utilisateur utilisateur = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + matricule));

        // Recalculer le rôle du préfixe matricule
        String role = RoleExtractor.extractRole(matricule);

        // Récupérer le siteId à jour
        Integer siteId = null;
        if (utilisateur.getSiteAssociated() != null) {
            siteId = utilisateur.getSiteAssociated().getSiteId();
        }

        // Générer un nouveau access token avec infos à jour
        return jwtUtil.generateToken(matricule, role, siteId);
    }

    public String register(AuthController.RegisterRequest request) {
        String role = request.accountType();
        if ("LOCALADMIN".equals(role) || "GLOBALADMIN".equals(role)) {
            throw new IllegalArgumentException("Le register public ne permet pas de créer des admins");
        }

        return registerInternal(request);
    }

    public String registerAdmin(AuthController.RegisterRequest request) {
        String role = request.accountType();
        if (!"LOCALADMIN".equals(role) && !"GLOBALADMIN".equals(role)) {
            throw new IllegalArgumentException("Seuls les comptes admins peuvent être créés via cet endpoint");
        }

        return registerInternal(request);
    }

    private String registerInternal(AuthController.RegisterRequest request) {
        String role = request.accountType();
        validateRequest(request, role);

        String matricule = generateMatricule(role);

        Utilisateur user = new Utilisateur();
        user.setMatricule(matricule);
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        user.setPassword(passwordEncoder.encode(request.password()));

        if ("Site".equals(role) || "LOCALADMIN".equals(role)) {
            Site site = siteRepository.findFirstByNomIgnoreCase(request.ville().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Site introuvable pour la ville : " + request.ville()));
            user.setSiteAssociated(site);
        }

        utilisateurRepository.save(user);
        return matricule;
    }

    private void validateRequest(AuthController.RegisterRequest req, String role) {
        if (isBlank(req.nom()) || isBlank(req.prenom())
                || isBlank(req.password()) || isBlank(role)) {
            throw new IllegalArgumentException("Tous les champs obligatoires doivent être remplis.");
        }
        if (("Site".equals(role) || "LOCALADMIN".equals(role)) && isBlank(req.ville())) {
            throw new IllegalArgumentException("La ville est requise pour un compte local.");
        }
    }

    private String generateMatricule(String role) {
        String prefix = switch (role) {
            case "Free"        -> "L";
            case "GLOBAL"      -> "G";
            case "Site"        -> "S";
            case "LOCALADMIN"  -> "LA";
            case "GLOBALADMIN" -> "GA";
            default -> throw new IllegalArgumentException("Type de compte inconnu : " + role);
        };
        long count = utilisateurRepository.countByMatriculeStartingWith(prefix) + 1;
        return prefix + String.format("%05d", count);
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}
