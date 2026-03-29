package be.ephec.padel_backend.repository;
import be.ephec.padel_backend.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Integer> {
    Optional<Site> findFirstByNomIgnoreCase(String nom);
}
