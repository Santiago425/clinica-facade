package co.edu.ucc.clinic.dto;

import java.util.List;

public record AppointmentConfirmation(String patientName, AppointmentView appointment, List<String> reminders) {
}
