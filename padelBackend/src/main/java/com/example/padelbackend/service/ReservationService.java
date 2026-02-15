package com.example.padelbackend.service;

import java.sql.Time;
import java.util.Date;

public class ReservationService {
    Time timeDebut = Time.valueOf("08:00:00");
    Time timeFin = Time.valueOf("22:00:00");

    public boolean isValidReservation(Date date, Time time) {
        Date currentDate = new Date();
        if (date.before(currentDate)) {
            return false; // La date de réservation est dans le passé
        }
        if (time.before(timeDebut) || time.after(timeFin)) {
            return false; // L'heure de réservation est en dehors des heures d'ouverture
        }
        return true; // La réservation est valide
    }


}
