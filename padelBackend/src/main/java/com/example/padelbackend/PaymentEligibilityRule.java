package com.example.padelbackend;

public interface PaymentEligibilityRule {
    boolean canPay(User user, Reservation reservation);
}
