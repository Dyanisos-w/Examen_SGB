package be.ephec.padel_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "site_closures", uniqueConstraints = {
    @UniqueConstraint(name = "uq_site_closure", columnNames = {"site_id", "date_debut", "date_fin"})
})
public class SiteClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "motif")
    private String motif;

    // true = créée via "appliquer à tous les sites"
    @Column(name = "is_global", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean global = false;

}

