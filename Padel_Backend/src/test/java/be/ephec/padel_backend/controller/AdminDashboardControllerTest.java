package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.DashboardOverviewDto;
import be.ephec.padel_backend.DTO.admin.DashboardMemberRowDto;
import be.ephec.padel_backend.DTO.admin.DashboardReservationRowDto;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.AdminDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private AdminAccessService adminAccessService;

    @Mock
    private AdminDashboardService adminDashboardService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminDashboardController controller;

    @Test
    void shouldReturnOverviewForGlobalAdmin() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();

        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        DashboardOverviewDto dto = new DashboardOverviewDto(50, 1250.0, 12, 61.5, 8.0);

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);
        when(adminDashboardService.getOverview(scope, "7d", null)).thenReturn(dto);

        var response = controller.getOverview(userDetails, request, "7d", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().getTotalReservations());
        assertEquals(1250.0, response.getBody().getTotalRevenue());
        assertEquals(12, response.getBody().getTotalUsers());
        assertEquals(61.5, response.getBody().getOccupancyRate());
        assertEquals(8.0, response.getBody().getCancellationRate());
    }

    @Test
    void shouldReturnReservationsForLocalAdmin() {
        UserDetails userDetails = User.withUsername("LA12345")
                .password("encoded")
                .authorities("ROLE_LOCALADMIN")
                .build();

        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(false, 1, "ROLE_LOCALADMIN");
        DashboardReservationRowDto row = new DashboardReservationRowDto(
                1,
                LocalDate.of(2026, 4, 10),
                LocalTime.of(18, 0),
                LocalTime.of(19, 30),
                "CONFIRMED",
                "PUBLIC",
                32.0,
                "L00001",
                "Nom Prenom",
                2,
                "Terrain 2",
                1,
                "Bruxelles"
        );

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);
        when(adminDashboardService.getReservations(scope, "7d")).thenReturn(List.of(row));

        var response = controller.getReservations(userDetails, request, "7d");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().getFirst().siteId());
    }

    @Test
    void shouldReturnMembersForGlobalAdmin() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();

        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        DashboardMemberRowDto member = new DashboardMemberRowDto("L00001", "Dupont", "Alice", 1, "Bruxelles", null);

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);
        when(adminDashboardService.getMembers(scope)).thenReturn(List.of(member));

        var response = controller.getMembers(userDetails, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("L00001", response.getBody().getFirst().matricule());
    }
}

