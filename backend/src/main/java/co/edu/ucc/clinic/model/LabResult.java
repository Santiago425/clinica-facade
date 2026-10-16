package co.edu.ucc.clinic.model;

import java.time.LocalDateTime;
import java.util.List;

public record LabResult(
        Long id,
        Long patientId,
        ExamType exam,
        String examName,
        LocalDateTime requestedAt,
        List<LabValue> values,
        boolean allWithinRange
) {
}
