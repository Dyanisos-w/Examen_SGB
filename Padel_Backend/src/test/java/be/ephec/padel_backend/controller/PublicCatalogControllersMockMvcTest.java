package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Terrain;
import be.ephec.padel_backend.repository.SiteRepository;
import be.ephec.padel_backend.repository.TerrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicCatalogControllersMockMvcTest {

    @Mock
    private SiteRepository siteRepository;
    @Mock
    private TerrainRepository terrainRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SiteController siteController = new SiteController(siteRepository);
        TerrainController terrainController = new TerrainController(terrainRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(siteController, terrainController).build();
    }

    @Test
    void getSites_shouldReturnJsonArray() throws Exception {
        Site site = new Site();
        site.setSiteId(1);
        site.setNom("Bruxelles");

        when(siteRepository.findAll()).thenReturn(List.of(site));

        mockMvc.perform(get("/sites"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].siteId").value(1))
                .andExpect(jsonPath("$[0].nom").value("Bruxelles"));
    }

    @Test
    void getSiteById_shouldReturnJsonObject() throws Exception {
        Site site = new Site();
        site.setSiteId(2);
        site.setNom("Namur");

        when(siteRepository.findById(2)).thenReturn(Optional.of(site));

        mockMvc.perform(get("/sites/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.siteId").value(2))
                .andExpect(jsonPath("$.nom").value("Namur"));
    }

    @Test
    void getTerrains_shouldReturnJsonArray() throws Exception {
        Site site = new Site();
        site.setSiteId(1);

        Terrain terrain = new Terrain(10, "Terrain 1", site);
        when(terrainRepository.findBySiteSiteId(1)).thenReturn(List.of(terrain));

        mockMvc.perform(get("/terrains").param("siteId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].terrainId").value(10))
                .andExpect(jsonPath("$[0].nom").value("Terrain 1"))
                .andExpect(jsonPath("$[0].siteId").value(1));
    }
}

