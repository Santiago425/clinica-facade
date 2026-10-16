package co.edu.ucc.clinic.model;

public enum Specialty {
    GENERAL_MEDICINE("Medicina General"),
    CARDIOLOGY("Cardiología"),
    PEDIATRICS("Pediatría"),
    DERMATOLOGY("Dermatología");

    private final String label;

    Specialty(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
