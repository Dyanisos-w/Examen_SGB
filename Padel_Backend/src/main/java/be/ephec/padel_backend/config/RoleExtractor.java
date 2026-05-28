package be.ephec.padel_backend.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour déterminer les rôles Spring Security à partir du préfixe du matricule.
 *
 * Mapping :
 * - GA → ROLE_GLOBALADMIN
 * - LA → ROLE_LOCALADMIN
 * - G  → ROLE_GLOBALUSER
 * - L  → ROLE_FREEUSER
 * - S  → ROLE_SITEUSER
 */
public class RoleExtractor {

    private RoleExtractor() {
        // Classe utilitaire, ne pas instancier
    }

    /**
     * Extrait le rôle principal basé sur le préfixe du matricule.
     *
     * @param matricule le matricule de l'utilisateur
     * @return le rôle Spring Security correspondant (ex: "ROLE_GLOBALADMIN")
     * @throws IllegalArgumentException si le préfixe n'est pas reconnu
     */
    public static String extractRole(String matricule) {
        if (matricule == null || matricule.isEmpty()) {
            throw new IllegalArgumentException("Matricule invalide: " + matricule);
        }

        String prefix = extractPrefix(matricule);

        return switch (prefix) {
            case "GA" -> "ROLE_GLOBALADMIN";
            case "LA" -> "ROLE_LOCALADMIN";
            case "G" -> "ROLE_GLOBALUSER";
            case "L" -> "ROLE_FREEUSER";
            case "S" -> "ROLE_SITEUSER";
            default -> throw new IllegalArgumentException("Préfixe matricule non reconnu: " + prefix);
        };
    }

    /**
     * Extrait tous les rôles associés au matricule (principal + implicites).
     * Exemple: GA → [ROLE_GLOBALADMIN, ROLE_LOCALADMIN, ROLE_GLOBALUSER, ROLE_FREEUSER, ROLE_SITEUSER]
     *
     * @param matricule le matricule de l'utilisateur
     * @return liste des rôles
     */
    public static List<String> extractAllRoles(String matricule) {
        List<String> roles = new ArrayList<>();
        String mainRole = extractRole(matricule);
        roles.add(mainRole);

        // Ajouter les rôles implicites (hiérarchie)
        if ("ROLE_GLOBALADMIN".equals(mainRole)) {
            roles.add("ROLE_LOCALADMIN");
            roles.add("ROLE_GLOBALUSER");
            roles.add("ROLE_FREEUSER");
            roles.add("ROLE_SITEUSER");
        } else if ("ROLE_LOCALADMIN".equals(mainRole)) {
            roles.add("ROLE_FREEUSER");
            roles.add("ROLE_SITEUSER");
        } else if ("ROLE_GLOBALUSER".equals(mainRole)) {
            roles.add("ROLE_FREEUSER");
            roles.add("ROLE_SITEUSER");
        } else if ("ROLE_FREEUSER".equals(mainRole)) {
            roles.add("ROLE_SITEUSER");
        }

        return roles;
    }

    /**
     * Extrait le préfixe du matricule (GA, LA, G, L, ou S).
     *
     * @param matricule le matricule complet
     * @return le préfixe (2 ou 1 caractère)
     */
    private static String extractPrefix(String matricule) {
        if (matricule.startsWith("GA") || matricule.startsWith("LA")) {
            return matricule.substring(0, 2);
        } else if (matricule.length() >= 1) {
            return matricule.substring(0, 1);
        }
        return "";
    }

    /**
     * Vérifie si le matricule correspond à un administrateur (GA ou LA).
     *
     * @param matricule le matricule
     * @return true si global ou local admin, false sinon
     */
    public static boolean isAdmin(String matricule) {
        String role = extractRole(matricule);
        return "ROLE_GLOBALADMIN".equals(role) || "ROLE_LOCALADMIN".equals(role);
    }

    /**
     * Vérifie si le matricule est un administrateur global.
     *
     * @param matricule le matricule
     * @return true si global admin, false sinon
     */
    public static boolean isGlobalAdmin(String matricule) {
        return "ROLE_GLOBALADMIN".equals(extractRole(matricule));
    }

    /**
     * Vérifie si le matricule est un administrateur local.
     *
     * @param matricule le matricule
     * @return true si local admin, false sinon
     */
    public static boolean isLocalAdmin(String matricule) {
        return "ROLE_LOCALADMIN".equals(extractRole(matricule));
    }
}

