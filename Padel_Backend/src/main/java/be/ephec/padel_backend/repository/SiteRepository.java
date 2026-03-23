package be.ephec.padel_backend.repository;
import be.ephec.padel_backend.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Integer> {
}
