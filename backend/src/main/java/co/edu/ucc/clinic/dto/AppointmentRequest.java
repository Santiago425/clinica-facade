package co.edu.ucc.clinic.dto;

import java.time.LocalDateTime;

public record AppointmentRequest(Long patientId, String specialty, Long doctorId, LocalDateTime dateTime) {
}
