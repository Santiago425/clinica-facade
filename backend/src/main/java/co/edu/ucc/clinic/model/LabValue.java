package co.edu.ucc.clinic.model;

public record LabValue(
        String parameter,
        double value,
        String unit,
        double minReference,
        double maxReference,
        boolean withinRange
) {
}
