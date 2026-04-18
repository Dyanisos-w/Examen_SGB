package be.ephec.padel_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE  = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String ROLE_CLAIM = "role";
    private static final String SITE_ID_CLAIM = "siteId";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Génère un token d'accès avec matricule, rôle et siteId.
     * 
     * @param matricule l'identifiant unique de l'utilisateur
     * @param role le rôle ROLE_* extrait du préfixe matricule
     * @param siteId l'ID du site associé (peut être null pour les admins globaux)
     * @return le JWT signé
     */
    public String generateToken(String matricule, String role, Integer siteId) {
        var builder = Jwts.builder()
                .subject(matricule)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim(ROLE_CLAIM, role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey());

        if (siteId != null) {
            builder.claim(SITE_ID_CLAIM, siteId);
        }

        return builder.compact();
    }

    /**
     * Génère un token d'accès avec rôle et siteId extraits automatiquement du matricule.
     * Compatible avec l'interface UserDetails existante pour les refresh tokens.
     * 
     * @param userDetails l'utilisateur Spring Security
     * @return le JWT signé
     */
    public String generateToken(UserDetails userDetails) {
        String matricule = userDetails.getUsername();
        String role = RoleExtractor.extractRole(matricule);
        return generateToken(matricule, role, null);
    }

    /**
     * Génère un refresh token à partir de UserDetails.
     *
     * @param userDetails l'utilisateur Spring Security
     * @return le refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(userDetails.getUsername());
    }

    /**
     * Génère un refresh token à partir du matricule (surcharge).
     * Utilisé dans AuthService pour éviter de charger UserDetails inutilement.
     *
     * @param matricule l'identifiant unique
     * @return le refresh token
     */
    public String generateRefreshToken(String matricule) {
        return Jwts.builder()
                .subject(matricule)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrait le rôle ROLE_* du token JWT.
     * 
     * @param token le JWT
     * @return le rôle (ex: "ROLE_GLOBALADMIN")
     */
    public String extractRole(String token) {
        return getClaims(token).get(ROLE_CLAIM, String.class);
    }

    /**
     * Extrait l'ID du site associé du token JWT (peut être null).
     * 
     * @param token le JWT
     * @return l'ID du site ou null
     */
    public Integer extractSiteId(String token) {
        Object siteId = getClaims(token).get(SITE_ID_CLAIM);
        if (siteId == null) {
            return null;
        }
        return ((Number) siteId).intValue();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

