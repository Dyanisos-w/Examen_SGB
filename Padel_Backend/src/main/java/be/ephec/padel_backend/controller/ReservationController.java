package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.CreateReservationRequestDto;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /** Créer une réservation */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody CreateReservationRequestDto req,
            @AuthenticationPrincipal UserDetails user) {

        Reservation reservation = reservationService.createReservation(
                user.getUsername(),          // userId depuis le JWT
                req.terrainId,
                req.siteId,
                LocalDate.parse(req.date),
                LocalTime.parse(req.heureDebut),
                req.typeReservation
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation.getIdReservation());
    }

    /** Mes réservations */
    @GetMapping("/me")
    public ResponseEntity<List<Reservation>> getMyReservations(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.getUserReservations(user.getUsername()));
    }

    /** Réservations publiques disponibles */
    @GetMapping("/public")
    public ResponseEntity<List<Reservation>> getPublicReservations(
            @RequestParam(required = false) Integer siteId) {
        Integer id = siteId != null ? siteId : 1;
        return ResponseEntity.ok(reservationService.getPublicReservations(id));
    }

    /** Rejoindre une réservation publique */
    @PostMapping("/{id}/join")
    public ResponseEntity<Void> join(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        reservationService.joinPublicReservation(id, user.getUsername());
        return ResponseEntity.ok().build();
    }

    /** Ajouter un joueur à une réservation privée */
    @PostMapping("/{id}/players")
    public ResponseEntity<Void> addPlayer(
            @PathVariable Integer id,
            @RequestBody AddPlayerRequest req,
            @AuthenticationPrincipal UserDetails user) {
        reservationService.addPlayerToPrivate(id, user.getUsername(), req.joueurId());
        return ResponseEntity.ok().build();
    }

    /** Quitter une réservation */
    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leave(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        reservationService.leaveReservation(id, user.getUsername());
        return ResponseEntity.ok().build();
    }

    /** Annuler une réservation (organisateur) */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        reservationService.cancelReservation(id, user.getUsername());
        return ResponseEntity.ok().build();
    }

    /** Payer sa part */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> pay(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        reservationService.payerReservation(id, user.getUsername());
        return ResponseEntity.ok().build();
    }

    public record AddPlayerRequest(String joueurId) {}
}
