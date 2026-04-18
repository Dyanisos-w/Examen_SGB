package be.ephec.padel_backend.DTO;

public class ParticipantPaymentDto {
    public String matricule;
    public String nom;
    public String prenom;
    public String paymentStatus; // "A_PAYER" or "PAYE"
    public boolean isMe;
}

