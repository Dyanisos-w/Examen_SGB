package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Site;
import be.ephec.padel_backend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    long countByMatriculeStartingWith(String prefix);
    Optional<Utilisateur> findByMatricule(String matricule);

    @Query("SELECT COUNT(u) FROM Utilisateur u " +
           "WHERE UPPER(u.matricule) LIKE 'G%' " +
           "OR UPPER(u.matricule) LIKE 'S%' " +
           "OR UPPER(u.matricule) LIKE 'L%'")
    long countPlayers();

    @Query("SELECT COUNT(u) FROM Utilisateur u " +
           "WHERE u.siteAssociated.siteId = :siteId " +
           "AND (UPPER(u.matricule) LIKE 'G%' " +
           "OR UPPER(u.matricule) LIKE 'S%' " +
           "OR UPPER(u.matricule) LIKE 'L%')")
    long countPlayersBySiteId(@Param("siteId") Integer siteId);

    @Query("SELECT u FROM Utilisateur u " +
           "WHERE UPPER(u.matricule) NOT LIKE 'GA%' " +
           "AND UPPER(u.matricule) NOT LIKE 'LA%' " +
           "ORDER BY u.nom, u.prenom")
    List<Utilisateur> findMembers();

    @Query("SELECT u FROM Utilisateur u LEFT JOIN u.siteAssociated s " +
           "WHERE (s IS NULL OR s.siteId = :siteId) " +
           "AND UPPER(u.matricule) NOT LIKE 'GA%' " +
           "AND UPPER(u.matricule) NOT LIKE 'LA%' " +
           "ORDER BY CASE WHEN s IS NOT NULL THEN 0 ELSE 1 END, u.nom, u.prenom")
    List<Utilisateur> findMembersBySiteId(@Param("siteId") Integer siteId);

    @Query("SELECT u FROM Utilisateur u " +
           "WHERE UPPER(u.matricule) LIKE 'GA%' " +
           "OR UPPER(u.matricule) LIKE 'LA%' " +
           "ORDER BY u.nom, u.prenom")
    List<Utilisateur> findAdmins();

    @Query("SELECT u FROM Utilisateur u " +
           "WHERE UPPER(u.matricule) LIKE 'LA%' " +
           "AND u.siteAssociated.siteId = :siteId " +
           "ORDER BY u.nom, u.prenom")
    List<Utilisateur> findAdminsBySiteId(@Param("siteId") Integer siteId);
}
