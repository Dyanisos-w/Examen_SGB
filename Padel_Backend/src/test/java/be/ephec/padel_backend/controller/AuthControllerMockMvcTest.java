package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.config.JwtUtil;
import be.ephec.padel_backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerMockMvcTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authenticationManager, jwtUtil, userDetailsService, authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void login_shouldReturn200AndTokens_whenCredentialsAreValid() throws Exception {
        when(authService.login("GA00001", "Admin123"))
                .thenReturn(new AuthController.LoginToken("access-token", "refresh-token"));

        var request = new AuthController.LoginRequest("GA00001", "Admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_shouldReturn401_whenServiceRejectsCredentials() throws Exception {
        when(authService.login(eq("GA00001"), eq("wrong-pass")))
                .thenThrow(new IllegalArgumentException("Bad credentials"));

        var request = new AuthController.LoginRequest("GA00001", "wrong-pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldReturn200AndMatricule_whenRequestIsValid() throws Exception {
        when(authService.register(any(AuthController.RegisterRequest.class)))
                .thenReturn("G00042");

        var request = new AuthController.RegisterRequest("Doe", "Jane", "secret123", "GLOBAL", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value("G00042"));
    }

    @Test
    void register_shouldReturn400_whenServiceRejectsRequest() throws Exception {
        when(authService.register(any(AuthController.RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("invalid input"));

        var request = new AuthController.RegisterRequest("Admin", "Global", "secret123", "GLOBALADMIN", "Bruxelles");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
