package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.SiteClosure;
import be.ephec.padel_backend.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SiteClosureRepository extends JpaRepository<SiteClosure, Long> {
    // Fermetures actives sur une date pour un site (inclut les globales rattachées au site)
    @Query("SELECT sc FROM SiteClosure sc WHERE sc.site = :site AND :date BETWEEN sc.dateDebut AND sc.dateFin")
    List<SiteClosure> findSiteClosuresOnDate(@Param("site") Site site, @Param("date") LocalDate date);

    // Compat ancienne donnée : fermetures globales avec site IS NULL actives sur une date
    @Query("SELECT sc FROM SiteClosure sc WHERE sc.site IS NULL AND :date BETWEEN sc.dateDebut AND sc.dateFin")
    List<SiteClosure> findLegacyGlobalClosuresOnDate(@Param("date") LocalDate date);

    // Toutes les fermetures d'un site sur une période (inclut globales rattachées)
    @Query("SELECT sc FROM SiteClosure sc WHERE sc.site = :site AND sc.dateFin >= :start AND sc.dateDebut <= :end")
    List<SiteClosure> findClosuresForSiteAndPeriod(@Param("site") Site site, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Fermetures globales (is_global = true) triées par date de début
    List<SiteClosure> findByGlobalTrueOrderByDateDebutAsc();

    // Fermetures d'un site spécifique, triées par date de début
    List<SiteClosure> findBySite_SiteIdOrderByDateDebutAsc(Integer siteId);

    // Vérifie si une fermeture identique existe déjà pour un site
    boolean existsBySiteAndDateDebutAndDateFin(Site site, LocalDate dateDebut, LocalDate dateFin);

    // Supprime toutes les fermetures globales d'une même période (pour delete global)
    @Modifying
    @Query("DELETE FROM SiteClosure sc WHERE sc.dateDebut = :dateDebut AND sc.dateFin = :dateFin AND sc.global = true")
    void deleteAllGlobalClosuresForPeriod(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
}

