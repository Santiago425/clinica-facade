package co.edu.ucc.clinic.facade;

import co.edu.ucc.clinic.dto.AppointmentConfirmation;
import co.edu.ucc.clinic.dto.AppointmentView;
import co.edu.ucc.clinic.dto.ConsultationRequest;
import co.edu.ucc.clinic.dto.DoctorAvailability;
import co.edu.ucc.clinic.dto.FullHistoryResponse;
import co.edu.ucc.clinic.dto.LabResponse;
import co.edu.ucc.clinic.dto.PatientProfile;
import co.edu.ucc.clinic.dto.PrescriptionResponse;
import co.edu.ucc.clinic.dto.RegisterPatientRequest;
import co.edu.ucc.clinic.dto.SpecialtyView;
import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.model.Appointment;
import co.edu.ucc.clinic.model.Consultation;
import co.edu.ucc.clinic.model.ExamType;
import co.edu.ucc.clinic.model.LabResult;
import co.edu.ucc.clinic.model.Medication;
import co.edu.ucc.clinic.model.Patient;
import co.edu.ucc.clinic.model.Prescription;
import co.edu.ucc.clinic.model.Specialty;
import co.edu.ucc.clinic.service.LaboratoryService;
import co.edu.ucc.clinic.service.MedicalHistoryService;
import co.edu.ucc.clinic.service.PatientService;
import co.edu.ucc.clinic.service.PrescriptionService;
import co.edu.ucc.clinic.service.ScheduleService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ClinicFacade {

    private final PatientService patientService;
    private final ScheduleService scheduleService;
    private final MedicalHistoryService medicalHistoryService;
    private final PrescriptionService prescriptionService;
    private final LaboratoryService laboratoryService;

    public ClinicFacade(PatientService patientService,
                        ScheduleService scheduleService,
                        MedicalHistoryService medicalHistoryService,
                        PrescriptionService prescriptionService,
                        LaboratoryService laboratoryService) {
        this.patientService = patientService;
        this.scheduleService = scheduleService;
        this.medicalHistoryService = medicalHistoryService;
        this.prescriptionService = prescriptionService;
        this.laboratoryService = laboratoryService;
    }

    public PatientProfile registerPatient(RegisterPatientRequest request) {
        Patient patient = patientService.register(request);
        medicalHistoryService.openRecord(patient.id(), request.allergies());
        return PatientProfile.of(patient, medicalHistoryService.getAllergies(patient.id()));
    }

    public PatientProfile login(String document, String password) {
        Patient patient = patientService.authenticate(document, password);
        return PatientProfile.of(patient, medicalHistoryService.getAllergies(patient.id()));
    }

    public PatientProfile getProfile(Long patientId) {
        Patient patient = patientService.getProfile(patientId);
        return PatientProfile.of(patient, medicalHistoryService.getAllergies(patient.id()));
    }

    public AppointmentConfirmation scheduleAppointment(Long patientId, String specialty, LocalDateTime date) {
        return scheduleAppointment(patientId, specialty, null, date);
    }

    public AppointmentConfirmation scheduleAppointment(Long patientId, String specialty, Long doctorId, LocalDateTime date) {
        Patient patient = patientService.getProfile(patientId);
        Specialty parsedSpecialty = scheduleService.parseSpecialty(specialty);
        Appointment appointment = scheduleService.book(patient.id(), parsedSpecialty, doctorId, date);
        List<String> reminders = scheduleService.buildReminders(appointment);
        if (!medicalHistoryService.getAllergies(patient.id()).isEmpty()) {
            reminders = new ArrayList<>(reminders);
            reminders.add("Informe al médico sus alergias registradas: "
                    + String.join(", ", medicalHistoryService.getAllergies(patient.id())) + ".");
        }
        return new AppointmentConfirmation(patient.fullName(), AppointmentView.of(appointment), reminders);
    }

    public AppointmentView cancelAppointment(Long patientId, Long appointmentId) {
        Patient patient = patientService.getProfile(patientId);
        return AppointmentView.of(scheduleService.cancel(appointmentId, patient.id()));
    }

    public FullHistoryResponse getFullHistory(Long patientId) {
        Patient patient = patientService.getProfile(patientId);
        List<String> allergies = medicalHistoryService.getAllergies(patient.id());
        List<AppointmentView> upcoming = scheduleService.getUpcomingAppointments(patient.id()).stream()
                .map(AppointmentView::of)
                .toList();
        List<AppointmentView> past = scheduleService.getPastAppointments(patient.id()).stream()
                .map(AppointmentView::of)
                .toList();
        List<Consultation> consultations = medicalHistoryService.getConsultations(patient.id());
        List<Prescription> prescriptions = prescriptionService.getPrescriptions(patient.id());
        List<LabResult> labResults = laboratoryService.getResults(patient.id());
        return new FullHistoryResponse(
                PatientProfile.of(patient, allergies),
                allergies,
                medicalHistoryService.getDiagnoses(patient.id()),
                upcoming,
                past,
                consultations,
                prescriptions,
                labResults
        );
    }

    public PrescriptionResponse generatePrescription(Long patientId, List<Medication> medications, String instructions) {
        Patient patient = patientService.getProfile(patientId);
        if (medications == null || medications.isEmpty()) {
            throw new BusinessException("La prescripción debe tener al menos un medicamento");
        }
        List<String> allergies = medicalHistoryService.getAllergies(patient.id());
        List<String> conflicts = prescriptionService.findAllergyConflicts(medications, allergies);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("No se puede generar la prescripción. Medicamentos contraindicados: "
                    + String.join(", ", conflicts));
        }
        Prescription prescription = prescriptionService.generate(patient.id(), medications, instructions);
        return new PrescriptionResponse(patient.fullName(), allergies, prescription);
    }

    public LabResponse requestExams(Long patientId, List<String> exams) {
        Patient patient = patientService.getProfile(patientId);
        List<ExamType> examTypes = laboratoryService.parseExams(exams);
        List<LabResult> results = laboratoryService.request(patient.id(), examTypes);
        int outOfRange = (int) results.stream()
                .flatMap(result -> result.values().stream())
                .filter(value -> !value.withinRange())
                .count();
        return new LabResponse(patient.fullName(), results, outOfRange);
    }

    public Consultation registerConsultation(ConsultationRequest request) {
        if (request == null) {
            throw new BusinessException("Los datos de la consulta son obligatorios");
        }
        Appointment appointment = scheduleService.getAppointment(request.appointmentId());
        Patient patient = patientService.getProfile(appointment.getPatientId());
        if (request.diagnosis() == null || request.diagnosis().isBlank()) {
            throw new BusinessException("El diagnóstico es obligatorio");
        }
        scheduleService.markAttended(appointment.getId());
        return medicalHistoryService.registerConsultation(
                patient.id(),
                appointment.getId(),
                appointment.getDateTime(),
                appointment.getDoctor().name(),
                appointment.getDoctor().specialty().getLabel(),
                request.reason(),
                request.diagnosis(),
                request.notes()
        );
    }

    public List<String> addAllergy(Long patientId, String allergy) {
        Patient patient = patientService.getProfile(patientId);
        return medicalHistoryService.addAllergy(patient.id(), allergy);
    }

    public List<DoctorAvailability> getDoctors(String specialty) {
        Specialty parsed = specialty == null || specialty.isBlank() ? null : scheduleService.parseSpecialty(specialty);
        return scheduleService.getAvailability(parsed);
    }

    public List<SpecialtyView> getSpecialties() {
        return scheduleService.getSpecialties().stream()
                .map(s -> new SpecialtyView(s.name(), s.getLabel()))
                .toList();
    }

    public List<Map<String, String>> getAvailableExams() {
        return laboratoryService.getAvailableExams().stream()
                .map(exam -> Map.of("code", exam.name(), "name", exam.getLabel()))
                .toList();
    }
}
