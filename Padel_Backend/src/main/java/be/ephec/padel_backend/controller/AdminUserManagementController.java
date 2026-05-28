package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.service.AuthService;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.AdminUserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminAccessService adminAccessService;
    private final AuthService authService;
    private final AdminUserManagementService adminUserManagementService;

    @PostMapping(value = "/admins", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthController.RegisterResponse> createAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestBody AuthController.RegisterRequest registerRequest) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        if (!scope.global()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul GLOBALADMIN peut créer un admin");
        }

        try {
            String matricule = authService.registerAdmin(registerRequest);
            return ResponseEntity.ok(new AuthController.RegisterResponse(matricule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/admins/{matricule}")
    public ResponseEntity<Void> revokeLocalAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @PathVariable String matricule) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        if (!scope.global()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul GLOBALADMIN peut révoquer un admin");
        }
        adminUserManagementService.revokeLocalAdmin(matricule);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/members/{matricule}/ban")
    public ResponseEntity<Void> banMember(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @PathVariable String matricule) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        adminUserManagementService.banMember(matricule, scope);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/members/{matricule}/ban")
    public ResponseEntity<Void> unbanMember(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @PathVariable String matricule) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        adminUserManagementService.unbanMember(matricule, scope);
        return ResponseEntity.noContent().build();
    }
}
