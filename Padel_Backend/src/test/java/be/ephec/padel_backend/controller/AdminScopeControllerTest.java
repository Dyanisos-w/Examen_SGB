package be.ephec.padel_backend.controller;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminScopeControllerTest {

    @Mock
    private AdminAccessService adminAccessService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminScopeController controller;

    @Test
    void shouldReturnGlobalScopePayload() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();

        when(adminAccessService.resolveScope(userDetails, request))
                .thenReturn(new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN"));

        var response = controller.getScope(userDetails, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROLE_GLOBALADMIN", response.getBody().role());
        assertEquals(true, response.getBody().global());
        assertEquals(null, response.getBody().siteId());
    }
}
