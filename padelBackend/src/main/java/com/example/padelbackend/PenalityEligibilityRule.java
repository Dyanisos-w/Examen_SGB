package com.example.padelbackend;

public interface PenalityEligibilityRule {
    boolean hasActivePenalty(User user);
    void applyPenalty(User user);
}
