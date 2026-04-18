package be.ephec.padel_backend.config;

import be.ephec.padel_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration de la sécurité avec authentification basée sur la BD MSSQL.
 *
 * UserDetailsService charge les utilisateurs depuis la table Utilisateur
 * et construit les autorités basées sur le préfixe du matricule.
 */
@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UtilisateurRepository utilisateurRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Service pour charger les détails utilisateur depuis la BD.
     * Le rôle est déterminé dynamiquement à partir du préfixe matricule.
     *
     * @return UserDetailsService qui requête la BD
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Charger utilisateur depuis BD par matricule
            return utilisateurRepository.findByMatricule(username)
                    .map(utilisateur -> {
                        // Construire un UserDetails Spring avec le matricule et password BD
                        return org.springframework.security.core.userdetails.User.builder()
                                .username(utilisateur.getMatricule())
                                .password(utilisateur.getPassword())
                                .authorities(RoleExtractor.extractAllRoles(utilisateur.getMatricule())
                                        .stream()
                                        .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r))
                                        .toArray(org.springframework.security.core.GrantedAuthority[]::new))
                                .accountExpired(false)
                                .accountLocked(false)
                                .credentialsExpired(false)
                                .disabled(false)
                                .build();
                    })
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
        };
    }
}

