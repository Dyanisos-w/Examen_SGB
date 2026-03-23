package be.ephec.padel_backend.service;

import be.ephec.padel_backend.DTO.CreateReservationRequest;
import be.ephec.padel_backend.DTO.ReservationDto;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    // plus tard remplacer appeller la stored procedure
    public ReservationDto createReservation(CreateReservationRequest req) {
        ReservationDto dto = new ReservationDto();
        dto.id = 1;
        dto.date = req.date;
        dto.startTime = req.startTime;
        dto.endTime = "22:30";
        dto.siteName = "Nivelles";
        dto.terrainName = "Terrain 1";

        return dto;
    }
}
