package be.ephec.padel_backend.service;

import be.ephec.padel_backend.controller.AuthController;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceInvalidInputTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldThrowWhenNameIsBlank() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                " ", "Alice", "secret123", "Site", "Bruxelles"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertTrue(ex.getMessage().contains("obligatoires"));
        verifyNoInteractions(utilisateurRepository, siteRepository, passwordEncoder);
    }

    @Test
    void shouldThrowWhenPublicRegisterRequestsAdminRole() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Admin", "Global", "secret123", "GLOBALADMIN", "Bruxelles"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertTrue(ex.getMessage().contains("register public"));
        verifyNoInteractions(utilisateurRepository, siteRepository, passwordEncoder);
    }
}

