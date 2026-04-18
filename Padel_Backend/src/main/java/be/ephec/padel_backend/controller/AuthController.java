package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.config.JwtUtil;
import be.ephec.padel_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    /**
     * Endpoint de login : authentifie l'utilisateur et génère JWT avec rôle + siteId.
     * Matricule = username, password = mot de passe en BD.
     *
     * @param request contenant matricule et password
     * @return LoginToken avec accessToken et refreshToken
     */
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginToken> login(@RequestBody LoginRequest request) {
        try {
            // Utiliser AuthService pour charger depuis BD et générer JWT enrichi
            LoginToken token = authService.login(request.username(), request.password());
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(value = "/refresh", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.refreshToken();
        try {
            // Vérifier que c'est un refresh token valide
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Extraire le matricule du refresh token
            String matricule = jwtUtil.extractUsername(refreshToken);
            
            // Vérifier la validité avec UserDetailsService (signature + expiration)
            UserDetails userDetails = userDetailsService.loadUserByUsername(matricule);
            if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Recharger l'utilisateur depuis la BD et régénérer accessToken avec infos à jour
            String newAccessToken = authService.refreshAccessToken(matricule);
            return ResponseEntity.ok(new RefreshResponse(newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            String matricule = authService.register(request);
            return ResponseEntity.ok(new RegisterResponse(matricule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    public record RegisterRequest(
            String nom,
            String prenom,
            String password,
            String accountType,
            String ville
    ) {}

    public record RegisterResponse(String matricule) {}

    public record LoginRequest(String username, String password) {}

    public record LoginToken(String accessToken, String refreshToken) {}

    public record LoginResponse(String accessToken, String refreshToken) {}

    public record RefreshRequest(String refreshToken) {}

    public record RefreshResponse(String accessToken) {}
}

