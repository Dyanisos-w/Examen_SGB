package be.ephec.padel_backend.service;

import be.ephec.padel_backend.controller.AuthController;
import be.ephec.padel_backend.model.Site;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterLocalUserTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterLocalUserSuccessfully() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Dupont", "Alice", "secret123", "Site", "Bruxelles"
        );

        Site site = new Site();
        site.setSiteId(10);
        site.setNom("Bruxelles");

        when(utilisateurRepository.findMaxNumeroByPrefix("S", 1)).thenReturn(Optional.empty());
        when(siteRepository.findFirstByNomIgnoreCase("Bruxelles")).thenReturn(Optional.of(site));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

        String matricule = authService.register(request);

        assertEquals("S00001", matricule);
        assertTrue(matricule.startsWith("S"));

        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());

        Utilisateur saved = userCaptor.getValue();
        assertEquals("Alice", saved.getPrenom());
        assertEquals("Dupont", saved.getNom());
        assertEquals("hashed-password", saved.getPassword());
        assertEquals(site, saved.getSiteAssociated());

        verify(utilisateurRepository).findMaxNumeroByPrefix("S", 1);
        verify(siteRepository).findFirstByNomIgnoreCase("Bruxelles");
        verify(passwordEncoder).encode("secret123");
        verifyNoMoreInteractions(utilisateurRepository, siteRepository, passwordEncoder);
    }
}

