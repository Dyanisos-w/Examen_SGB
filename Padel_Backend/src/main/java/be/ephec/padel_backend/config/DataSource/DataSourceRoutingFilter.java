package be.ephec.padel_backend.config.DataSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class DataSourceRoutingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()) {

                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().contains("ADMIN"));

                if (isAdmin) {
                    DataSourceContext.set(DataSourceType.ADMIN);
                } else {
                    DataSourceContext.set(DataSourceType.USER);
                }
            } else {
                DataSourceContext.set(DataSourceType.USER);
            }

            filterChain.doFilter(request, response);

        } finally {
            DataSourceContext.clear(); // 🔥 ultra important
        }
    }
}