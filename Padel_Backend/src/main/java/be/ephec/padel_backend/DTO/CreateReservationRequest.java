package be.ephec.padel_backend.DTO;

public class CreateReservationRequest {
    public Integer siteId;
    public Integer terrainId;
    public String date;
    public String startTime;
    public boolean isPrivate;
    public Integer userId;
}
