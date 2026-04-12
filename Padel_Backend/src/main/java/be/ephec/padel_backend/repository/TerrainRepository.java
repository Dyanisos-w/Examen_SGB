package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerrainRepository extends JpaRepository<Terrain, Integer> {

    List<Terrain> findBySiteSiteId(Integer siteId);

    boolean existsBySiteSiteIdAndNomIgnoreCase(Integer siteId, String nom);
}
