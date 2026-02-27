package com.example.padelbackend.repository;

import com.example.padelbackend.model.Matchs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MatchsRepository {
    private final JdbcTemplate jdbcTemplate;
    public MatchsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void createMatch(Matchs match) {
        String sql = "EXEC CreateMatch ?, ?, ?, ?, ?, ?, ?";
        jdbcTemplate.update(sql,
                match.getSiteId(),
                match.getReservationId(),
                match.getTerrainId());
    }

    public List<Matchs> getAllMatchs() {

        return jdbcTemplate.query(
                "EXEC GetAllMatchs",
                this::mapRowToMatch
        );
    }

    public Matchs getMatchById(Integer id) {
        return jdbcTemplate.queryForObject(
                "EXEC GetMatchById ?",
                new Object[]{id},
                this::mapRowToMatch
        );
    }

    private Matchs mapRowToMatch(ResultSet rs, int rowNum) throws SQLException {
        Matchs match = new Matchs();
        match.setId(rs.getInt("ID"));
        match.setSiteId(rs.getInt("SiteID"));
        match.setReservationId(rs.getInt("ReservationIDReservation"));
        match.setTerrainId(rs.getInt("TerrainID_terrain"));

        return match;
    }

}
