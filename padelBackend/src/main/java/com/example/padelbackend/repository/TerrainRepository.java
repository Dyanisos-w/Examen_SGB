package com.example.padelbackend.repository;

import com.example.padelbackend.model.Terrain;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TerrainRepository {
    private final JdbcTemplate jdbcTemplate;

    public TerrainRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTerrain (Terrain terrain) {
        String sql = "EXEC CreateTerrain ?";
        jdbcTemplate.update(sql, terrain.getSiteId());
    }

    public List<Terrain> getTerrainsBySite(Integer siteId) {

        return jdbcTemplate.query(
                "EXEC GetTerrainsBySite ?",
                new Object[]{siteId},
                this::mapRowToTerrain
        );
    }


    public Terrain getTerrainByID (Integer id){
        return jdbcTemplate.queryForObject(
                "EXEC GetTerrainById ?",
                new Object[]{id},
                this::mapRowToTerrain
        );
    }

    private Terrain mapRowToTerrain(ResultSet rs, int rowNum) throws SQLException {

        Terrain terrain = new Terrain();
        terrain.setIdTerrain(rs.getInt("ID_terrain"));
        terrain.setSiteId(rs.getInt("SiteID"));

        return terrain;
    }
}
