package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.service.AuthService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementControllerTest {

    @Mock
    private AdminAccessService adminAccessService;
    @Mock
    private AuthService authService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminUserManagementController controller;

    @Test
    void shouldAllowGlobalAdminToCreateAdmin() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");

        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest(
                "Martin", "Claire", "secret123", "LOCALADMIN", "Bruxelles"
        );

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);
        when(authService.registerAdmin(registerRequest)).thenReturn("LA00042");

        var response = controller.createAdmin(userDetails, request, registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LA00042", response.getBody().matricule());
    }

    @Test
    void shouldRejectLocalAdminCreateAdmin() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(false, 1, "ROLE_LOCALADMIN");

        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest(
                "Admin", "Local", "secret123", "LOCALADMIN", "Bruxelles"
        );

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createAdmin(userDetails, request, registerRequest));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}


