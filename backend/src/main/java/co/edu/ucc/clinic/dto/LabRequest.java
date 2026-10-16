package co.edu.ucc.clinic.dto;

import java.util.List;

public record LabRequest(Long patientId, List<String> exams) {
}
