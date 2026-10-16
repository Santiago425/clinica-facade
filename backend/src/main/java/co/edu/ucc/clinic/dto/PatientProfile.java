package co.edu.ucc.clinic.dto;

import co.edu.ucc.clinic.model.Patient;

import java.time.LocalDate;
import java.util.List;

public record PatientProfile(
        Long id,
        String document,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        List<String> allergies
) {
    public static PatientProfile of(Patient patient, List<String> allergies) {
        return new PatientProfile(
                patient.id(),
                patient.document(),
                patient.firstName(),
                patient.lastName(),
                patient.email(),
                patient.phone(),
                patient.birthDate(),
                allergies
        );
    }
}
