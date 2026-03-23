package be.ephec.padel_backend.repository;

import be.ephec.padel_backend.model.ReservationUtilisateur;
import be.ephec.padel_backend.model.ReservationUtilisateurId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationUtilisateurRepository
        extends JpaRepository<ReservationUtilisateur, ReservationUtilisateurId> {
}
