package com.example.padelbackend.repository;

import com.example.padelbackend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    private final JdbcTemplate jdbcTemplate;

    public UtilisateurRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createUtilisateur(String matricule,String nom, String prenom, String email) {
        String sql = "EXE CreateUtilisateur ?, ?, ?, ?";
        jdbcTemplate.update(sql,matricule, nom, prenom, email);
    }

    public Utilisateur findByMatricule(String matricule) {

        String sql = "EXE GetUtilisateurByMatricule ?";

        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{matricule},
                this::mapRowToUtilisateur
        );
    }


    private Utilisateur mapRowToUtilisateur(ResultSet rs, int rowNum) throws SQLException {
        return  new Utilisateur(
                rs.getString("matricule"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("hash")
        );

        
    }
}
