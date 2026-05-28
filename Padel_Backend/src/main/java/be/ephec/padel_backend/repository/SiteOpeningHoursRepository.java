package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.SiteOpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SiteOpeningHoursRepository extends JpaRepository<SiteOpeningHours, Integer> {

    List<SiteOpeningHours> findBySiteSiteId(Integer siteId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SiteOpeningHours s WHERE s.site.siteId = :siteId")
    void deleteBySiteSiteId(@Param("siteId") Integer siteId);
}

