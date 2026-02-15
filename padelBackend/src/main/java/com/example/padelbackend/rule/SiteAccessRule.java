package com.example.padelbackend.rule;
// This interface defines the contract for determining if a user can reserve a site based on certain rules.
// rules if matricule begin by G | L | S  the rule should be Global |Libre | Site respectively.
public interface SiteAccessRule {

        boolean canReserveOnSite(User user, Site site);


}
