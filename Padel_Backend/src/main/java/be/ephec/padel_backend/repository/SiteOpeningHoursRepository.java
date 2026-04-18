package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.SiteOpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteOpeningHoursRepository extends JpaRepository<SiteOpeningHours, Integer> {

    List<SiteOpeningHours> findBySiteSiteId(Integer siteId);

    void deleteBySiteSiteId(Integer siteId);
}

