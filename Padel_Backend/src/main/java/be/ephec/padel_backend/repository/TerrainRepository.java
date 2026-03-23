package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerrainRepository extends JpaRepository<Terrain, Integer> {
}
