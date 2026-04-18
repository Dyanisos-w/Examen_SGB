package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.service.admin.AdminAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSiteManagementControllerMockMvcTest {

    @Mock
    private AdminAccessService adminAccessService;
    @Mock
    private SiteRepository siteRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminSiteManagementController controller = new AdminSiteManagementController(adminAccessService, siteRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createSite_shouldIgnoreNombreTerrainsFromJsonPayload() throws Exception {
        when(adminAccessService.resolveScope(any(), any()))
                .thenReturn(new AdminAccessService.AdminScope(true, null, "ROLE_GLOBALADMIN"));

        when(siteRepository.save(any(Site.class))).thenAnswer(invocation -> {
            Site toSave = invocation.getArgument(0);
            toSave.setSiteId(42);
            return toSave;
        });

        String payload = """
                {
                  "nom": "  Bruxelles  ",
                  "adresse": "  Rue du Test 10  ",
                  "nombreTerrains": 999
                }
                """;

        mockMvc.perform(post("/api/admin/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.siteId").value(42))
                .andExpect(jsonPath("$.nom").value("Bruxelles"))
                .andExpect(jsonPath("$.adresse").value("Rue du Test 10"))
                .andExpect(jsonPath("$.nombreTerrains").value(0));

        ArgumentCaptor<Site> siteCaptor = ArgumentCaptor.forClass(Site.class);
        verify(siteRepository).save(siteCaptor.capture());
        Site savedSite = siteCaptor.getValue();

        assertEquals("Bruxelles", savedSite.getNom());
        assertEquals("Rue du Test 10", savedSite.getAdresse());
        assertEquals(0, savedSite.getNombreTerrains());
    }
}

