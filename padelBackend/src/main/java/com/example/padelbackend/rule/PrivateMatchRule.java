package com.example.padelbackend.rule;
// This interface defines the contract for determining if a match is Private based on certain rules.
public interface PrivateMatchRule {
    boolean isPrivateMatchValid(Match match);
}
