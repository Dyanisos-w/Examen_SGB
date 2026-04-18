package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSiteManagementControllerTest {

    @Mock
    private AdminAccessService adminAccessService;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminSiteManagementController controller;

    @Test
    void shouldAllowGlobalAdminToCreateSite() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();

        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        AdminSiteManagementController.CreateSiteRequest dto =
                new AdminSiteManagementController.CreateSiteRequest("Bruxelles", "Rue du Test 10");

        Site saved = new Site();
        saved.setSiteId(10);
        saved.setNom("Bruxelles");
        saved.setAdresse("Rue du Test 10");
        saved.setNombreTerrains(0);

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);
        when(siteRepository.save(any(Site.class))).thenReturn(saved);

        var response = controller.createSite(userDetails, request, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10, response.getBody().getSiteId());
        assertEquals("Bruxelles", response.getBody().getNom());
        assertEquals(0, response.getBody().getNombreTerrains());
    }

    @Test
    void shouldRejectLocalAdminCreateSite() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();

        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(false, 1, "ROLE_LOCALADMIN");
        AdminSiteManagementController.CreateSiteRequest dto =
                new AdminSiteManagementController.CreateSiteRequest("Liege", "Rue X");

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createSite(userDetails, request, dto));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}

