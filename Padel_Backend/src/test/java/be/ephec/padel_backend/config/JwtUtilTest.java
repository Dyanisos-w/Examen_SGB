package be.ephec.padel_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    @Test
    void shouldGenerateAccessAndRefreshJwtTokens() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 120000L);

        UserDetails user = User.withUsername("L00001")
                .password("x")
                .roles("FREEUSER")
                .build();

        String access = jwtUtil.generateToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        assertFalse(access.isBlank());
        assertFalse(refresh.isBlank());
        assertEquals("L00001", jwtUtil.extractUsername(access));
        assertTrue(jwtUtil.isTokenValid(access, user));
        assertTrue(jwtUtil.isRefreshToken(refresh));
        assertFalse(jwtUtil.isRefreshToken(access));
    }
}
