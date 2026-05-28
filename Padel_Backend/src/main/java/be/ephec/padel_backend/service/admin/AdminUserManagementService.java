package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final UtilisateurRepository utilisateurRepository;

    public void revokeLocalAdmin(String matricule) {
        Utilisateur u = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!u.getMatricule().toUpperCase().startsWith("LA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce compte n'est pas un admin local");
        }
        utilisateurRepository.delete(u);
    }

    public void banMember(String matricule, AdminAccessService.AdminScope scope) {
        Utilisateur u = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!scope.global()) {
            Integer userSiteId = u.getSiteAssociated() != null ? u.getSiteAssociated().getSiteId() : null;
            if (!scope.siteId().equals(userSiteId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "LOCALADMIN limité à son propre site");
            }
        }
        u.setInterditReservationJusqua(LocalDate.of(9999, 12, 31));
        utilisateurRepository.save(u);
    }

    public void unbanMember(String matricule, AdminAccessService.AdminScope scope) {
        Utilisateur u = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!scope.global()) {
            Integer userSiteId = u.getSiteAssociated() != null ? u.getSiteAssociated().getSiteId() : null;
            if (!scope.siteId().equals(userSiteId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "LOCALADMIN limité à son propre site");
            }
        }
        u.setInterditReservationJusqua(null);
        utilisateurRepository.save(u);
    }
}
