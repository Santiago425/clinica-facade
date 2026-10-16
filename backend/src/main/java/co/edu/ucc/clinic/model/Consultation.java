package co.edu.ucc.clinic.model;

import java.time.LocalDateTime;

public record Consultation(
        Long id,
        Long patientId,
        Long appointmentId,
        LocalDateTime date,
        String doctorName,
        String specialty,
        String reason,
        String diagnosis,
        String notes
) {
}
