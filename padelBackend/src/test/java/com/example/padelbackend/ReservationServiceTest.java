package com.example.padelbackend;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {
    public static void main(String[] args) {
        ReservationService service = new ReservationService();

        User user = new User("G1234"); // Global
        Site site = new Site("Bruxelles");
        Terrain terrain = new Terrain("Terrain 1");

        CreateReservationRequest request =
                new CreateReservationRequest(
                        LocalDate.now().plusDays(30),
                        site,
                        terrain,
                        ReservationType.PUBLIC
                );

        // --- Exécution ---
        try {
            service.createReservation(request, user);
            System.out.println("TEST OK : réservation créée");
        } catch (Exception e) {
            System.out.println("TEST KO : " + e.getMessage());
        }
    }

}