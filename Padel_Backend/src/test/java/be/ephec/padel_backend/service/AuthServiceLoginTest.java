package be.ephec.padel_backend.service;

import be.ephec.padel_backend.config.JwtUtil;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    private static final String DEV_PASSWORD_HASH = "$2a$10$me8nmJMwqQIhdeOSONlZ/uen64fIqbS/884EJt/TJBXjFpx364hR2";

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(utilisateurRepository, siteRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void shouldLoginWithDefaultDevAdminCredentials() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setMatricule("GA00001");
        utilisateur.setPassword(DEV_PASSWORD_HASH);

        when(utilisateurRepository.findByMatricule("GA00001")).thenReturn(Optional.of(utilisateur));
        when(jwtUtil.generateToken("GA00001", "ROLE_GLOBALADMIN", null)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("GA00001")).thenReturn("refresh-token");

        var response = authService.login("GA00001", "Admin123");

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void shouldRejectLoginWhenPasswordIsInvalid() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setMatricule("GA00001");
        utilisateur.setPassword(DEV_PASSWORD_HASH);

        when(utilisateurRepository.findByMatricule("GA00001")).thenReturn(Optional.of(utilisateur));

        assertThrows(IllegalArgumentException.class, () -> authService.login("GA00001", "wrong-pass"));
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExist() {
        when(utilisateurRepository.findByMatricule("GA00001")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login("GA00001", "Admin123"));
    }
}

