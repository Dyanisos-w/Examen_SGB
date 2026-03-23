package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.CreateReservationRequest;
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
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest req) {

        reservationService.createReservation(
                req.userId,
                req.siteId,
                req.terrainId,
                LocalDate.parse(req.date),
                LocalTime.parse(req.heureDebut)
        );

        return ResponseEntity.ok().build();
    }
}