package com.example.padelbackend.rule;

public interface ReservationVisibilityRule {
    boolean isReservationVisibleToUser(Reservation reservation, User user);
}
