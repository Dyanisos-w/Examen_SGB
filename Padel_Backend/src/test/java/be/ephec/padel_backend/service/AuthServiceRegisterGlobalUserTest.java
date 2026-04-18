package be.ephec.padel_backend.service;

import be.ephec.padel_backend.controller.AuthController;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterGlobalUserTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterGlobalUserWithoutCity() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Dupont", "Alice", "secret123", "GLOBAL", null
        );

        when(utilisateurRepository.countByMatriculeStartingWith("G")).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

        String matricule = authService.register(request);

        assertEquals("G00001", matricule);
        assertTrue(matricule.startsWith("G"));

        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());

        Utilisateur saved = userCaptor.getValue();
        assertEquals("Alice", saved.getPrenom());
        assertEquals("Dupont", saved.getNom());
        assertEquals("hashed-password", saved.getPassword());
        assertNull(saved.getSiteAssociated());

        verify(utilisateurRepository).countByMatriculeStartingWith("G");
        verify(siteRepository, never()).findFirstByNomIgnoreCase(org.mockito.ArgumentMatchers.anyString());
        verify(passwordEncoder).encode("secret123");
        verifyNoMoreInteractions(utilisateurRepository, siteRepository, passwordEncoder);
    }
}

