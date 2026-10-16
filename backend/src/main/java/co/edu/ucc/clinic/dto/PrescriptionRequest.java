package co.edu.ucc.clinic.dto;

import co.edu.ucc.clinic.model.Medication;

import java.util.List;

public record PrescriptionRequest(Long patientId, List<Medication> medications, String instructions) {
}
