package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.SiteClosureAdminRequestDto;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.SiteClosureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/closures")
@RequiredArgsConstructor
public class SiteClosureAdminController {
    private final SiteClosureService siteClosureService;
    private final AdminAccessService adminAccessService;

    @PostMapping
    public ResponseEntity<Void> applyClosure(@AuthenticationPrincipal UserDetails userDetails,
                                             HttpServletRequest request,
                                             @RequestBody SiteClosureAdminRequestDto closureRequest) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        siteClosureService.applyClosure(scope, closureRequest);
        return ResponseEntity.noContent().build();
    }
}


