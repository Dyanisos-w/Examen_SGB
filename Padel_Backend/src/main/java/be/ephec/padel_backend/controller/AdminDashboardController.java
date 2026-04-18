package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.DashboardOverviewDto;
import be.ephec.padel_backend.DTO.admin.DashboardMemberRowDto;
import be.ephec.padel_backend.DTO.admin.DashboardReservationRowDto;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.AdminDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminAccessService adminAccessService;
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview(@AuthenticationPrincipal UserDetails userDetails,
                                                            HttpServletRequest request,
                                                            @RequestParam String period) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        DashboardOverviewDto overview = adminDashboardService.getOverview(scope, period);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<DashboardReservationRowDto>> getReservations(@AuthenticationPrincipal UserDetails userDetails,
                                                                            HttpServletRequest request,
                                                                            @RequestParam String period) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(adminDashboardService.getReservations(scope, period));
    }

    @GetMapping("/members")
    public ResponseEntity<List<DashboardMemberRowDto>> getMembers(@AuthenticationPrincipal UserDetails userDetails,
                                                                   HttpServletRequest request) {
        AdminAccessService.AdminScope scope = adminAccessService.resolveScope(userDetails, request);
        return ResponseEntity.ok(adminDashboardService.getMembers(scope));
    }
}

