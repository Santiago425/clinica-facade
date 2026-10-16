package co.edu.ucc.clinic.service;

import co.edu.ucc.clinic.config.ClinicClock;
import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.model.ExamType;
import co.edu.ucc.clinic.model.LabResult;
import co.edu.ucc.clinic.model.LabValue;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LaboratoryService {

    private record Reference(String parameter, String unit, double min, double max) {
    }

    private static final Map<ExamType, List<Reference>> REFERENCES = Map.of(
            ExamType.HEMOGRAM, List.of(
                    new Reference("Hemoglobina", "g/dL", 12.0, 17.5),
                    new Reference("Hematocrito", "%", 36.0, 50.0),
                    new Reference("Leucocitos", "x10³/µL", 4.5, 11.0),
                    new Reference("Plaquetas", "x10³/µL", 150.0, 450.0)
            ),
            ExamType.GLYCEMIA, List.of(
                    new Reference("Glucosa en ayunas", "mg/dL", 70.0, 100.0)
            ),
            ExamType.LIPID_PROFILE, List.of(
                    new Reference("Colesterol total", "mg/dL", 125.0, 200.0),
                    new Reference("Colesterol HDL", "mg/dL", 40.0, 60.0),
                    new Reference("Colesterol LDL", "mg/dL", 50.0, 130.0),
                    new Reference("Triglicéridos", "mg/dL", 50.0, 150.0)
            )
    );

    private final Map<Long, List<LabResult>> results = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);
    private final Random random = new Random();

    public List<ExamType> parseExams(List<String> exams) {
        if (exams == null || exams.isEmpty()) {
            throw new BusinessException("Debe seleccionar al menos un examen");
        }
        Set<ExamType> parsed = new LinkedHashSet<>();
        for (String exam : exams) {
            ExamType type = Arrays.stream(ExamType.values())
                    .filter(t -> t.name().equalsIgnoreCase(exam.trim()) || t.getLabel().equalsIgnoreCase(exam.trim()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("El examen " + exam + " no está disponible"));
            parsed.add(type);
        }
        return List.copyOf(parsed);
    }

    public synchronized List<LabResult> request(Long patientId, List<ExamType> exams) {
        LocalDateTime now = ClinicClock.now();
        List<LabResult> created = new ArrayList<>();
        for (ExamType exam : exams) {
            created.add(store(patientId, exam, now, simulateValues(exam)));
        }
        return created;
    }

    public synchronized LabResult registerPast(Long patientId, ExamType exam, LocalDateTime date, double... values) {
        List<Reference> references = REFERENCES.get(exam);
        List<LabValue> labValues = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            labValues.add(toValue(references.get(i), values[i]));
        }
        return store(patientId, exam, date, labValues);
    }

    public List<LabResult> getResults(Long patientId) {
        return results.getOrDefault(patientId, List.of()).stream()
                .sorted(Comparator.comparing(LabResult::requestedAt).reversed().thenComparing(LabResult::id))
                .toList();
    }

    public List<ExamType> getAvailableExams() {
        return Arrays.asList(ExamType.values());
    }

    private LabResult store(Long patientId, ExamType exam, LocalDateTime date, List<LabValue> values) {
        boolean allWithinRange = values.stream().allMatch(LabValue::withinRange);
        LabResult result = new LabResult(sequence.getAndIncrement(), patientId, exam, exam.getLabel(), date, values, allWithinRange);
        results.computeIfAbsent(patientId, key -> new ArrayList<>()).add(result);
        return result;
    }

    private List<LabValue> simulateValues(ExamType exam) {
        return REFERENCES.get(exam).stream()
                .map(reference -> toValue(reference, simulate(reference)))
                .toList();
    }

    private double simulate(Reference reference) {
        double span = reference.max() - reference.min();
        double value = random.nextDouble() < 0.7
                ? reference.min() + random.nextDouble() * span
                : (random.nextBoolean()
                ? reference.max() + random.nextDouble() * span * 0.35
                : reference.min() - random.nextDouble() * span * 0.25);
        return Math.round(Math.max(value, 0) * 10.0) / 10.0;
    }

    private LabValue toValue(Reference reference, double value) {
        boolean withinRange = value >= reference.min() && value <= reference.max();
        return new LabValue(reference.parameter(), value, reference.unit(), reference.min(), reference.max(), withinRange);
    }
}
