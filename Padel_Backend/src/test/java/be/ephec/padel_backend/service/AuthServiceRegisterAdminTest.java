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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterAdminTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterLocalAdminViaAdminFlow() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Martin", "Claire", "secret123", "LOCALADMIN", "Bruxelles"
        );

        Site site = new Site();
        site.setSiteId(1);
        site.setNom("Bruxelles");

        when(utilisateurRepository.findMaxNumeroByPrefix("LA", 2)).thenReturn(Optional.empty());
        when(siteRepository.findFirstByNomIgnoreCase("Bruxelles")).thenReturn(Optional.of(site));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        String matricule = authService.registerAdmin(request);

        assertEquals("LA00001", matricule);

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertEquals("LA00001", captor.getValue().getMatricule());
        assertEquals(site, captor.getValue().getSiteAssociated());
    }
}

