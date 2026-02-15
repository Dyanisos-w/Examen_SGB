package com.example.padelbackend.rule;

import java.time.LocalDate;

public interface ReservationCreationRule {
    Reservation createReservation(
            user Organizer,
            LocalDate date,
            Site site,
            Terrain terrain,
            reservation matchType
    );
}
