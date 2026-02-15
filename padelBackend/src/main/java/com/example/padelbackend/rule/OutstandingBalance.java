package com.example.padelbackend.rule;
// This interface defines the contract for checking if a user has an outstanding balance and retrieving that balance.
public interface OutstandingBalance {
    boolean hasOutstandingBalance(User user);
    double getOutstandingBalance(User user);
}

