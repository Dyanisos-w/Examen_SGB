package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.CreateReservationRequestDto;
import be.ephec.padel_backend.DTO.ReservationDto;
import be.ephec.padel_backend.model.Reservation;
import be.ephec.padel_backend.repository.ReservationRepository;
import be.ephec.padel_backend.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequestDto req) {

        Reservation reservation = reservationService.createReservation(
                req.userId,
                req.terrainId,
                req.siteId,
                LocalDate.parse(req.date),
                LocalTime.parse(req.heureDebut),
                req.typeReservation
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservation.getIdReservation());
    }
}