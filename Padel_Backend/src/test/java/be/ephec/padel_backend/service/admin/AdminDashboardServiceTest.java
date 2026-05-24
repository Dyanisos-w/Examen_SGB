package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.DTO.admin.DashboardOverviewDto;
import be.ephec.padel_backend.model.PaymentStatus;
import be.ephec.padel_backend.repository.PaymentRepository;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.repository.SiteOpeningHoursRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SiteOpeningHoursRepository siteOpeningHoursRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AdminDashboardService service;

    @Test
    void shouldComputePlayersFromPopulationForGlobalScope() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");

        when(reservationRepository.countBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(10L);
        when(reservationRepository.countCancelledBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(2L);
        when(utilisateurRepository.countPlayers()).thenReturn(25L);
        when(paymentRepository.sumBetween(any(LocalDate.class), any(LocalDate.class), eq(PaymentStatus.PAYE))).thenReturn(500.0);
        when(siteRepository.findAll()).thenReturn(Collections.emptyList());

        DashboardOverviewDto dto = service.getOverview(scope, "7d", null);

        assertEquals(25L, dto.getTotalUsers());
        assertEquals(500.0, dto.getTotalRevenue());
        verify(utilisateurRepository).countPlayers();
        verify(paymentRepository).sumBetween(any(LocalDate.class), any(LocalDate.class), eq(PaymentStatus.PAYE));
    }

    @Test
    void shouldComputePlayersFromPopulationForLocalScope() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(false, 3, "ROLE_LOCALADMIN");

        when(reservationRepository.countBetween(any(LocalDate.class), any(LocalDate.class), eq(3))).thenReturn(5L);
        when(reservationRepository.countCancelledBetween(any(LocalDate.class), any(LocalDate.class), eq(3))).thenReturn(1L);
        when(utilisateurRepository.countPlayersBySiteId(3)).thenReturn(7L);
        when(paymentRepository.sumBetween(any(LocalDate.class), any(LocalDate.class), eq(3), eq(PaymentStatus.PAYE))).thenReturn(120.0);

        DashboardOverviewDto dto = service.getOverview(scope, "7d", null);

        assertEquals(7L, dto.getTotalUsers());
        assertEquals(120.0, dto.getTotalRevenue());
        verify(utilisateurRepository).countPlayersBySiteId(3);
        verify(paymentRepository).sumBetween(any(LocalDate.class), any(LocalDate.class), eq(3), eq(PaymentStatus.PAYE));
    }

    @Test
    void shouldRejectLocalScopeWhenRequestingAnotherSite() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(false, 3, "ROLE_LOCALADMIN");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getOverview(scope, "7d", 9)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}

