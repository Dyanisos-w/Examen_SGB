package com.example.padelbackend.rule;
// This interface defines the contract for processing actions that need to be taken the day before a match occurs, such as unpayed .
public interface ReservationDeadlineRule {
    void processDayBeforeMatch(Reservation Reservation, LocalDate currentDate);
}
