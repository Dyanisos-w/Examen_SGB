package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.SiteOpeningHoursAdminRequestDto;
import be.ephec.padel_backend.DTO.admin.SiteOpeningHoursAdminResponseDto;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.SiteOpeningHoursAdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/opening-hours")
@RequiredArgsConstructor
public class SiteOpeningHoursAdminController {

    private final SiteOpeningHoursAdminService siteOpeningHoursAdminService;
    private final AdminAccessService adminAccessService;

    @GetMapping
    public ResponseEntity<SiteOpeningHoursAdminResponseDto> getOpeningHours(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestParam(required = false) Integer siteId) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(siteOpeningHoursAdminService.getOpeningHours(scope, siteId));
    }

    @PutMapping
    public ResponseEntity<SiteOpeningHoursAdminResponseDto> updateOpeningHours(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestParam(required = false) Integer siteId,
            @RequestBody SiteOpeningHoursAdminRequestDto openingHoursRequest) {

        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(siteOpeningHoursAdminService.updateOpeningHours(scope, siteId, openingHoursRequest));
    }
}

