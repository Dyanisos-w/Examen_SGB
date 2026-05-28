package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.SiteClosureAdminRequestDto;
import be.ephec.padel_backend.DTO.admin.SiteClosureAdminResponseDto;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.SiteClosureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/global")
    public ResponseEntity<List<SiteClosureAdminResponseDto>> getGlobalClosures(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(siteClosureService.getGlobalClosures(scope));
    }

    @GetMapping("/site")
    public ResponseEntity<List<SiteClosureAdminResponseDto>> getSiteClosures(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestParam(required = false) Integer siteId) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(siteClosureService.getSiteClosures(scope, siteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClosure(@AuthenticationPrincipal UserDetails userDetails,
                                              HttpServletRequest request,
                                              @PathVariable Long id) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        siteClosureService.deleteClosure(scope, id);
        return ResponseEntity.noContent().build();
    }
}


