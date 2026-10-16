package co.edu.ucc.clinic.dto;

import co.edu.ucc.clinic.model.Appointment;

import java.time.LocalDateTime;

public record AppointmentView(
        Long id,
        Long patientId,
        Long doctorId,
        String doctorName,
        String specialty,
        String specialtyName,
        String office,
        LocalDateTime dateTime,
        String status
) {
    public static AppointmentView of(Appointment appointment) {
        return new AppointmentView(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctor().id(),
                appointment.getDoctor().name(),
                appointment.getDoctor().specialty().name(),
                appointment.getDoctor().specialty().getLabel(),
                appointment.getDoctor().office(),
                appointment.getDateTime(),
                appointment.getStatus().name()
        );
    }
}
