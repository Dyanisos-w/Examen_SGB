package com.example.padelbackend.repository;

import com.example.padelbackend.model.Site;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SiteRepository {
    private final JdbcTemplate jdbcTaller ;

    public  SiteRepository(JdbcTemplate jdbcTaller) {
        this.jdbcTaller = jdbcTaller;
    }
    public void createSite(Site sitee) {
        String sql = "EXEC CreateSite ?, ?, ?, ?";

        jdbcTaller.update(sql, sitee.getNom(), sitee.getAdresse(), sitee.getHeureOuverture(), sitee.getHeureFermeture());

    }
}
