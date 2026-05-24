package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTerrainManagementControllerTest {

    @Mock
    private AdminAccessService adminAccessService;
    @Mock
    private TerrainRepository terrainRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminTerrainManagementController controller;

    @Test
    void shouldAllowGlobalAdminToCreateTerrain() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();

        Site site = new Site();
        site.setSiteId(10);
        site.setNom("Bruxelles");
        site.setNombreTerrains(2);

        Terrain terrain = new Terrain(55, "Terrain 3", site);

        when(adminAccessService.resolveScope(userDetails, request))
                .thenReturn(new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN"));
        when(siteRepository.findById(10)).thenReturn(Optional.of(site));
        when(terrainRepository.existsBySiteSiteIdAndNomIgnoreCase(10, "Terrain 3")).thenReturn(false);
        when(terrainRepository.save(any(Terrain.class))).thenReturn(terrain);
        when(siteRepository.save(any(Site.class))).thenReturn(site);

        var response = controller.createTerrain(
                userDetails,
                request,
                new AdminTerrainManagementController.CreateTerrainRequest("Terrain 3", 10)
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Terrain 3", response.getBody().nom());
        assertEquals(10, response.getBody().site().siteId());
        assertEquals(3, site.getNombreTerrains());
    }

    @Test
    void shouldAllowLocalAdminToCreateTerrainOnOwnSite() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();

        Site site = new Site();
        site.setSiteId(7);
        site.setNom("Namur");
        site.setNombreTerrains(1);

        Terrain terrain = new Terrain(88, "Terrain B", site);

        when(adminAccessService.resolveScope(userDetails, request))
                .thenReturn(new AdminAccessService.AdminScope(false, 7, "ROLE_LOCALADMIN"));
        when(siteRepository.findById(7)).thenReturn(Optional.of(site));
        when(terrainRepository.existsBySiteSiteIdAndNomIgnoreCase(7, "Terrain B")).thenReturn(false);
        when(terrainRepository.save(any(Terrain.class))).thenReturn(terrain);
        when(siteRepository.save(any(Site.class))).thenReturn(site);

        var response = controller.createTerrain(
                userDetails,
                request,
                new AdminTerrainManagementController.CreateTerrainRequest("Terrain B", 7)
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Terrain B", response.getBody().nom());
        assertEquals(2, site.getNombreTerrains());
    }

    @Test
    void shouldRejectLocalAdminCreateTerrainOnAnotherSite() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();

        when(adminAccessService.resolveScope(userDetails, request))
                .thenReturn(new AdminAccessService.AdminScope(false, 7, "ROLE_LOCALADMIN"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createTerrain(
                        userDetails,
                        request,
                        new AdminTerrainManagementController.CreateTerrainRequest("Terrain X", 9)
                ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void shouldAllowLocalAdminToDeleteTerrainOnOwnSiteAndDecrementCounter() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();

        Site site = new Site();
        site.setSiteId(7);
        site.setNom("Namur");
        site.setNombreTerrains(2);

        Terrain terrain = new Terrain(88, "Terrain B", site);

        when(adminAccessService.resolveScope(userDetails, request))
                .thenReturn(new AdminAccessService.AdminScope(false, 7, "ROLE_LOCALADMIN"));
        when(terrainRepository.findById(88)).thenReturn(Optional.of(terrain));
        when(siteRepository.save(any(Site.class))).thenReturn(site);

        var response = controller.deleteTerrain(userDetails, request, 88);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(1, site.getNombreTerrains());
        verify(terrainRepository).delete(terrain);
        verify(siteRepository).save(site);
    }

    @Test
    void shouldRejectLocalAdminDeleteTerrainOnAnotherSite() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();

        Site site = new Site();
        site.setSiteId(10);
        site.setNombreTerrains(1);

        Terrain terrain = new Terrain(55, "Terrain X", site);

        when(adminAccessService.resolveScope(userDetails, request))
                .thenReturn(new AdminAccessService.AdminScope(false, 7, "ROLE_LOCALADMIN"));
        when(terrainRepository.findById(55)).thenReturn(Optional.of(terrain));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.deleteTerrain(userDetails, request, 55));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(terrainRepository, never()).delete(any(Terrain.class));
    }
}

