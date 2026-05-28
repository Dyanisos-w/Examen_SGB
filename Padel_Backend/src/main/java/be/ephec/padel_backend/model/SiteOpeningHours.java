package be.ephec.padel_backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Représente les horaires d'ouverture d'un site pour un jour de la semaine.
 * Un site est considéré fermé ce jour-là si {@code heureOuverture} est null.
 * Contrainte : une seule entrée par couple (site, jour).
 */
@Getter
@Setter
@Entity
@Table(
        name = "site_opening_hours",
        uniqueConstraints = @UniqueConstraint(name = "uk_site_opening_hours_site_day", columnNames = {"site_id", "jour_semaine"})
)
public class SiteOpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "heure_ouverture")
    private LocalTime heureOuverture;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "heure_fermeture")
    private LocalTime heureFermeture;

    public LocalTime getOpeningTime() {
        return heureOuverture;
    }

    public void setOpeningTime(LocalTime heureOuverture) {
        this.heureOuverture = heureOuverture;
    }

    public LocalTime getClosingTime() {
        return heureFermeture;
    }

    public void setClosingTime(LocalTime heureFermeture) {
        this.heureFermeture = heureFermeture;
    }

}
