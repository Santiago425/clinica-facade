package co.edu.ucc.clinic.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DoctorAvailability(
        Long id,
        String name,
        String specialty,
        String specialtyName,
        String office,
        List<LocalDateTime> availableSlots
) {
}
