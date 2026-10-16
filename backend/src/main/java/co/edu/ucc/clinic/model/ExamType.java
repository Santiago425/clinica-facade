package co.edu.ucc.clinic.model;

public enum ExamType {
    HEMOGRAM("Hemograma"),
    GLYCEMIA("Glicemia"),
    LIPID_PROFILE("Perfil lipídico");

    private final String label;

    ExamType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
