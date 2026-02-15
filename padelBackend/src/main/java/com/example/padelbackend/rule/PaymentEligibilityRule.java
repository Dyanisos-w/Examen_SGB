package com.example.padelbackend.rule;

public interface PaymentEligibilityRule {
    boolean canPay(User user, Reservation reservation);
}
