package co.edu.ucc.clinic.dto;

public record ConsultationRequest(Long appointmentId, String reason, String diagnosis, String notes) {
}
