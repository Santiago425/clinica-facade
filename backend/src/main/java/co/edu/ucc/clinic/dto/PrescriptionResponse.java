package co.edu.ucc.clinic.dto;

import co.edu.ucc.clinic.model.Prescription;

import java.util.List;

public record PrescriptionResponse(String patientName, List<String> checkedAllergies, Prescription prescription) {
}
