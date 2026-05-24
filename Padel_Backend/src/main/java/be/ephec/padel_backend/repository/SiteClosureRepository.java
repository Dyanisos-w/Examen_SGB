package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.SiteClosure;
import be.ephec.padel_backend.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SiteClosureRepository extends JpaRepository<SiteClosure, Long> {
    // Fermetures globales actives sur une date
    @Query("SELECT sc FROM SiteClosure sc WHERE sc.site IS NULL AND :date BETWEEN sc.dateDebut AND sc.dateFin")
    List<SiteClosure> findGlobalClosuresOnDate(@Param("date") LocalDate date);

    // Fermetures spécifiques à un site actives sur une date
    @Query("SELECT sc FROM SiteClosure sc WHERE sc.site = :site AND :date BETWEEN sc.dateDebut AND sc.dateFin")
    List<SiteClosure> findSiteClosuresOnDate(@Param("site") Site site, @Param("date") LocalDate date);

    // Toutes les fermetures (globales ou site) sur une période
    @Query("SELECT sc FROM SiteClosure sc WHERE (sc.site IS NULL OR sc.site = :site) AND sc.dateFin >= :start AND sc.dateDebut <= :end")
    List<SiteClosure> findClosuresForSiteAndPeriod(@Param("site") Site site, @Param("start") LocalDate start, @Param("end") LocalDate end);
}

