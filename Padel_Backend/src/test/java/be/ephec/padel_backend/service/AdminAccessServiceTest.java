package be.ephec.padel_backend.service;

import be.ephec.padel_backend.service.admin.AdminAccessService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccessServiceTest {

    @Mock
    private HttpServletRequest request;

    private final AdminAccessService adminAccessService = new AdminAccessService();

    @Test
    void shouldResolveGlobalAdminWithoutSiteFilter() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);

        assertTrue(scope.global());
        assertNull(scope.siteId());
        assertEquals("ROLE_GLOBALADMIN", scope.role());
    }

    @Test
    void shouldResolveLocalAdminWithJwtSite() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();
        when(request.getAttribute("siteId")).thenReturn(7);

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);

        assertEquals(7, scope.siteId());
        assertEquals("ROLE_LOCALADMIN", scope.role());
        assertFalse(scope.global());
    }

    @Test
    void shouldRejectLocalAdminWithoutJwtSite() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();
        when(request.getAttribute("siteId")).thenReturn(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> adminAccessService.resolveScope(userDetails, request)
        );

        assertEquals(403, ex.getStatusCode().value());
    }
}
