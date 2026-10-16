package co.edu.ucc.clinic.dto;

import co.edu.ucc.clinic.model.Consultation;
import co.edu.ucc.clinic.model.LabResult;
import co.edu.ucc.clinic.model.Prescription;

import java.util.List;

public record FullHistoryResponse(
        PatientProfile patient,
        List<String> allergies,
        List<String> diagnoses,
        List<AppointmentView> upcomingAppointments,
        List<AppointmentView> pastAppointments,
        List<Consultation> consultations,
        List<Prescription> prescriptions,
        List<LabResult> labResults
) {
}
