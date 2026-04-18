package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.config.RoleExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAccessService {

    public AdminScope resolveScope(UserDetails userDetails, HttpServletRequest request) {
        String matricule = userDetails.getUsername();

        if (RoleExtractor.isGlobalAdmin(matricule)) {
            return new AdminScope(true, null, RoleExtractor.extractRole(matricule));
        }

        if (RoleExtractor.isLocalAdmin(matricule)) {
            Integer siteId = (Integer) request.getAttribute("siteId");
            if (siteId == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Aucun site associé à ce compte LOCALADMIN"
                );
            }

            return new AdminScope(false, siteId, RoleExtractor.extractRole(matricule));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès admin refusé");
    }

    public record AdminScope(boolean global, Integer siteId, String role) {
    }
}
