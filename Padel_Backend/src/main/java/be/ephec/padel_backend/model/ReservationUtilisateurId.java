package be.ephec.padel_backend.model;
import java.util.Objects;
import java.io.Serializable;


public class ReservationUtilisateurId implements Serializable{
    private Integer reservation;
    private String utilisateur;

    public ReservationUtilisateurId() {}

    public ReservationUtilisateurId(Integer reservation, String utilisateur) {
        this.reservation = reservation;
        this.utilisateur = utilisateur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationUtilisateurId)) return false;
        ReservationUtilisateurId that = (ReservationUtilisateurId) o;
        return Objects.equals(reservation, that.reservation) &&
                Objects.equals(utilisateur, that.utilisateur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservation, utilisateur);
    }
}
