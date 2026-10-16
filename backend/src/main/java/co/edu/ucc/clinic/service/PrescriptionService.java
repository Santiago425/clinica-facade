package co.edu.ucc.clinic.service;

import co.edu.ucc.clinic.config.ClinicClock;
import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.model.Medication;
import co.edu.ucc.clinic.model.Prescription;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PrescriptionService {

    private static final Map<String, List<String>> DRUG_FAMILIES = Map.of(
            "penicilina", List.of("penicilina", "amoxicilina", "ampicilina", "dicloxacilina", "oxacilina"),
            "sulfa", List.of("sulfametoxazol", "trimetoprim", "sulfadiazina"),
            "aines", List.of("ibuprofeno", "naproxeno", "diclofenaco", "aspirina", "acido acetilsalicilico", "ketorolaco"),
            "aspirina", List.of("aspirina", "acido acetilsalicilico"),
            "cefalosporina", List.of("cefalexina", "ceftriaxona", "cefuroxima", "cefazolina")
    );

    private final Map<Long, List<Prescription>> prescriptions = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<String> findAllergyConflicts(List<Medication> medications, List<String> allergies) {
        List<String> conflicts = new ArrayList<>();
        for (Medication medication : medications) {
            String drug = normalize(medication.name());
            for (String allergy : allergies) {
                String allergen = normalize(allergy);
                List<String> family = DRUG_FAMILIES.getOrDefault(allergen, List.of(allergen));
                boolean conflict = drug.contains(allergen) || family.stream().anyMatch(drug::contains);
                if (conflict) {
                    conflicts.add(medication.name() + " (alergia a " + allergy + ")");
                }
            }
        }
        return conflicts;
    }

    public synchronized Prescription generate(Long patientId, List<Medication> medications, String instructions) {
        validate(medications);
        long id = sequence.getAndIncrement();
        LocalDateTime issuedAt = ClinicClock.now();
        List<Medication> cleaned = medications.stream()
                .map(m -> new Medication(m.name().trim(), m.dose().trim(),
                        m.frequency() == null || m.frequency().isBlank() ? "Según indicación médica" : m.frequency().trim(),
                        m.duration().trim()))
                .toList();
        Prescription prescription = new Prescription(
                id,
                patientId,
                String.format("RX-%d-%04d", issuedAt.getYear(), id),
                issuedAt,
                cleaned,
                instructions == null || instructions.isBlank() ? "Tomar los medicamentos según la dosis indicada." : instructions.trim()
        );
        prescriptions.computeIfAbsent(patientId, key -> new ArrayList<>()).add(prescription);
        return prescription;
    }

    public synchronized Prescription registerPast(Long patientId, LocalDateTime issuedAt, List<Medication> medications, String instructions) {
        long id = sequence.getAndIncrement();
        Prescription prescription = new Prescription(id, patientId,
                String.format("RX-%d-%04d", issuedAt.getYear(), id), issuedAt, medications, instructions);
        prescriptions.computeIfAbsent(patientId, key -> new ArrayList<>()).add(prescription);
        return prescription;
    }

    public List<Prescription> getPrescriptions(Long patientId) {
        return prescriptions.getOrDefault(patientId, List.of()).stream()
                .sorted(Comparator.comparing(Prescription::issuedAt).reversed())
                .toList();
    }

    private void validate(List<Medication> medications) {
        if (medications == null || medications.isEmpty()) {
            throw new BusinessException("La prescripción debe tener al menos un medicamento");
        }
        for (Medication medication : medications) {
            if (isBlank(medication.name()) || isBlank(medication.dose()) || isBlank(medication.duration())) {
                throw new BusinessException("Cada medicamento debe tener nombre, dosis y duración");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
