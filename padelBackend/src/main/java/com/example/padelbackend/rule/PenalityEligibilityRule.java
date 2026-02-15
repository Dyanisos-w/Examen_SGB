package com.example.padelbackend.rule;

public interface PenalityEligibilityRule {
    boolean hasActivePenalty(User user);
    void applyPenalty(User user);
}
