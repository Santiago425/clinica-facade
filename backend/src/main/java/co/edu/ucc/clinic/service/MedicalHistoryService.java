package co.edu.ucc.clinic.service;

import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.model.Consultation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MedicalHistoryService {

    private final Map<Long, Set<String>> allergies = new ConcurrentHashMap<>();
    private final Map<Long, List<Consultation>> consultations = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public void openRecord(Long patientId, List<String> initialAllergies) {
        allergies.putIfAbsent(patientId, new LinkedHashSet<>());
        consultations.putIfAbsent(patientId, new ArrayList<>());
        if (initialAllergies != null) {
            initialAllergies.forEach(allergy -> addAllergy(patientId, allergy));
        }
    }

    public synchronized List<String> addAllergy(Long patientId, String allergy) {
        if (allergy == null || allergy.isBlank()) {
            throw new BusinessException("Debe indicar la alergia");
        }
        String normalized = capitalize(allergy.trim());
        Set<String> patientAllergies = allergies.computeIfAbsent(patientId, id -> new LinkedHashSet<>());
        boolean exists = patientAllergies.stream().anyMatch(a -> a.equalsIgnoreCase(normalized));
        if (!exists) {
            patientAllergies.add(normalized);
        }
        return getAllergies(patientId);
    }

    public List<String> getAllergies(Long patientId) {
        return List.copyOf(allergies.getOrDefault(patientId, Set.of()));
    }

    public synchronized Consultation registerConsultation(Long patientId, Long appointmentId, LocalDateTime date,
                                                          String doctorName, String specialty,
                                                          String reason, String diagnosis, String notes) {
        if (diagnosis == null || diagnosis.isBlank()) {
            throw new BusinessException("El diagnóstico es obligatorio");
        }
        Consultation consultation = new Consultation(
                sequence.getAndIncrement(),
                patientId,
                appointmentId,
                date,
                doctorName,
                specialty,
                reason == null || reason.isBlank() ? "Consulta de control" : reason.trim(),
                diagnosis.trim(),
                notes == null ? "" : notes.trim()
        );
        consultations.computeIfAbsent(patientId, id -> new ArrayList<>()).add(consultation);
        return consultation;
    }

    public List<Consultation> getConsultations(Long patientId) {
        return consultations.getOrDefault(patientId, List.of()).stream()
                .sorted(Comparator.comparing(Consultation::date).reversed())
                .toList();
    }

    public List<String> getDiagnoses(Long patientId) {
        return getConsultations(patientId).stream()
                .map(Consultation::diagnosis)
                .distinct()
                .toList();
    }

    private String capitalize(String value) {
        String lower = value.toLowerCase(Locale.forLanguageTag("es"));
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
