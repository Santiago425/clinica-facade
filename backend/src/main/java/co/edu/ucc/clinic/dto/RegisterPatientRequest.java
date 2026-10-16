package co.edu.ucc.clinic.dto;

import java.time.LocalDate;
import java.util.List;

public record RegisterPatientRequest(
        String document,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        String password,
        List<String> allergies
) {
}
