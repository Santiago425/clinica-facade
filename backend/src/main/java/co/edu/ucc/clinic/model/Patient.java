package co.edu.ucc.clinic.model;

import java.time.LocalDate;

public record Patient(
        Long id,
        String document,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        String password
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
