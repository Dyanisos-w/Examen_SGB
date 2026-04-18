package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.config.JwtUtil;
import be.ephec.padel_backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerLoginTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldLoginAndReturnJwtTokens() {
        when(authService.login("user", "password"))
                .thenReturn(new AuthController.LoginToken("access-token", "refresh-token"));

        var response = authController.login(new AuthController.LoginRequest("user", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access-token", response.getBody().accessToken());
        assertFalse(response.getBody().refreshToken().isBlank());
    }

    @Test
    void shouldLoginSuccessfully() {
        when(authService.login("alice", "plain-pass"))
                .thenReturn(new AuthController.LoginToken("jwt-alice", "refresh-alice"));

        var response = authController.login(new AuthController.LoginRequest("alice", "plain-pass"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-alice", response.getBody().accessToken());
        assertEquals("refresh-alice", response.getBody().refreshToken());
    }

    @Test
    void shouldRejectLoginWhenBadCredentials() {
        when(authService.login("alice", "wrong"))
                .thenThrow(new IllegalArgumentException("Bad credentials"));

        var response = authController.login(new AuthController.LoginRequest("alice", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldRejectRegisterWhenPublicRequestIsInvalid() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Admin", "Global", "secret123", "GLOBALADMIN", "Bruxelles"
        );

        when(authService.register(request)).thenThrow(new IllegalArgumentException("forbidden"));

        var response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

