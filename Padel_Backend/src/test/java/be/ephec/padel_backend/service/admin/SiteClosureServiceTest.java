package be.ephec.padel_backend.service.admin;

import be.ephec.padel_backend.DTO.admin.SiteClosureAdminRequestDto;
import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.SiteClosure;
import be.ephec.padel_backend.repository.SiteClosureRepository;
import be.ephec.padel_backend.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteClosureServiceTest {

    @Mock
    private SiteClosureRepository siteClosureRepository;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private SiteClosureService service;

    @Test
    void shouldApplyClosureOnLocalAdminSite() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(false, 7, "ROLE_LOCALADMIN");
        Site site = new Site();
        site.setSiteId(7);
        SiteClosureAdminRequestDto dto = new SiteClosureAdminRequestDto(
                99,
                true,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                "Maintenance"
        );

        when(siteRepository.findById(7)).thenReturn(Optional.of(site));

        service.applyClosure(scope, dto);

        ArgumentCaptor<SiteClosure> captor = ArgumentCaptor.forClass(SiteClosure.class);
        verify(siteClosureRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getSite().getSiteId());
        assertEquals(LocalDate.of(2026, 8, 1), captor.getValue().getDateDebut());
        assertEquals(LocalDate.of(2026, 8, 3), captor.getValue().getDateFin());
        assertEquals("Maintenance", captor.getValue().getMotif());
        verify(siteRepository).findById(7);
        verify(siteRepository, never()).findById(99);
    }

    @Test
    void shouldApplyGlobalClosureForGlobalAdmin() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        SiteClosureAdminRequestDto dto = new SiteClosureAdminRequestDto(
                null,
                true,
                LocalDate.of(2026, 12, 24),
                LocalDate.of(2026, 12, 26),
                "  "
        );

        service.applyClosure(scope, dto);

        ArgumentCaptor<SiteClosure> captor = ArgumentCaptor.forClass(SiteClosure.class);
        verify(siteClosureRepository).save(captor.capture());
        assertNull(captor.getValue().getSite());
        assertNull(captor.getValue().getMotif());
        verify(siteRepository, never()).findById(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void shouldApplyTargetedClosureForGlobalAdmin() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        Site site = new Site();
        site.setSiteId(3);
        SiteClosureAdminRequestDto dto = new SiteClosureAdminRequestDto(
                3,
                false,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 12),
                "Tournoi"
        );

        when(siteRepository.findById(3)).thenReturn(Optional.of(site));

        service.applyClosure(scope, dto);

        ArgumentCaptor<SiteClosure> captor = ArgumentCaptor.forClass(SiteClosure.class);
        verify(siteClosureRepository).save(captor.capture());
        assertEquals(3, captor.getValue().getSite().getSiteId());
    }

    @Test
    void shouldRejectGlobalAdminWithoutTarget() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        SiteClosureAdminRequestDto dto = new SiteClosureAdminRequestDto(
                null,
                false,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 1),
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.applyClosure(scope, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        SiteClosureAdminRequestDto dto = new SiteClosureAdminRequestDto(
                null,
                true,
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 1),
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.applyClosure(scope, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}

