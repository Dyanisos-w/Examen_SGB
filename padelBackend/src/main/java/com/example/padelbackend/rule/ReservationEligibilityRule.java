package com.example.padelbackend.rule;

public interface ReservationEligibilityRule {
    boolean canCreatMatch(User user);
}
