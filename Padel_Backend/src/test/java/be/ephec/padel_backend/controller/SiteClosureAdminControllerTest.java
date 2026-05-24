package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.admin.SiteClosureAdminRequestDto;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import be.ephec.padel_backend.service.admin.SiteClosureService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteClosureAdminControllerTest {

    @Mock
    private SiteClosureService siteClosureService;

    @Mock
    private AdminAccessService adminAccessService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SiteClosureAdminController controller;

    @Test
    void shouldReturnNoContentWhenApplyingClosure() {
        UserDetails userDetails = User.withUsername("GA12345")
                .password("encoded")
                .authorities("ROLE_GLOBALADMIN")
                .build();
        AdminAccessService.AdminScope scope = new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN");
        SiteClosureAdminRequestDto dto = new SiteClosureAdminRequestDto(
                null,
                true,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7),
                "Vacances"
        );

        when(adminAccessService.resolveScope(userDetails, request)).thenReturn(scope);

        var response = controller.applyClosure(userDetails, request, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(siteClosureService).applyClosure(scope, dto);
    }
}

