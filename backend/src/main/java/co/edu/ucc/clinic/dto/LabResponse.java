package co.edu.ucc.clinic.dto;

import co.edu.ucc.clinic.model.LabResult;

import java.util.List;

public record LabResponse(String patientName, List<LabResult> results, int outOfRangeCount) {
}
