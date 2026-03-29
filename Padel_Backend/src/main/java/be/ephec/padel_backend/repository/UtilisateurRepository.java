package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UtilisateurRepository extends JpaRepository<be.ephec.padel_backend.model.Utilisateur, String> {
    long countByMatriculeStartingWith(String prefix);
}
