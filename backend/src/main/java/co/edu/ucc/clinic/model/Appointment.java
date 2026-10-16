package co.edu.ucc.clinic.model;

import java.time.LocalDateTime;

public class Appointment {

    private final Long id;
    private final Long patientId;
    private final Doctor doctor;
    private final LocalDateTime dateTime;
    private AppointmentStatus status;

    public Appointment(Long id, Long patientId, Doctor doctor, LocalDateTime dateTime, AppointmentStatus status) {
        this.id = id;
        this.patientId = patientId;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
