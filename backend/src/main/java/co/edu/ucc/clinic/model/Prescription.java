package co.edu.ucc.clinic.model;

import java.time.LocalDateTime;
import java.util.List;

public record Prescription(
        Long id,
        Long patientId,
        String code,
        LocalDateTime issuedAt,
        List<Medication> medications,
        String instructions
) {
}
