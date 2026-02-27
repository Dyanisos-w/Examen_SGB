package com.example.padelbackend.model;

import java.util.*;
import java.time.LocalDate;

public class Utilisateur {

    List<Payement> payment;
    List<Reservation_Utilisateur> reservation_utilisateur;
    List<Site> site;
    private String matricule;
    private String nom;
    private String prenom;
    private int penaliterMontant;
    private LocalDate interdit_reservation_jusqua;
    private String hashPassword;


}
