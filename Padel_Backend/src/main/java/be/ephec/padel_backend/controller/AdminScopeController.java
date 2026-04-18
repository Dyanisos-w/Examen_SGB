package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.service.admin.AdminAccessService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminScopeController {

    private final AdminAccessService adminAccessService;

    @GetMapping("/scope")
    public ResponseEntity<ScopeResponse> getScope(@AuthenticationPrincipal UserDetails userDetails,
                                                  HttpServletRequest request) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(new ScopeResponse(scope.role(), scope.global(), scope.siteId()));
    }

    public record ScopeResponse(String role, boolean global, Integer siteId) {
    }
}
