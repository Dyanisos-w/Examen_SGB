package com.example.padelbackend;

public interface ReservationVisibilityRule {
    boolean isReservationVisibleToUser(Reservation reservation, User user);
}
