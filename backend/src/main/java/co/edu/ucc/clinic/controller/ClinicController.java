package co.edu.ucc.clinic.controller;

import co.edu.ucc.clinic.dto.AllergyRequest;
import co.edu.ucc.clinic.dto.AppointmentConfirmation;
import co.edu.ucc.clinic.dto.AppointmentRequest;
import co.edu.ucc.clinic.dto.AppointmentView;
import co.edu.ucc.clinic.dto.ConsultationRequest;
import co.edu.ucc.clinic.dto.DoctorAvailability;
import co.edu.ucc.clinic.dto.FullHistoryResponse;
import co.edu.ucc.clinic.dto.LabRequest;
import co.edu.ucc.clinic.dto.LabResponse;
import co.edu.ucc.clinic.dto.LoginRequest;
import co.edu.ucc.clinic.dto.PatientProfile;
import co.edu.ucc.clinic.dto.PrescriptionRequest;
import co.edu.ucc.clinic.dto.PrescriptionResponse;
import co.edu.ucc.clinic.dto.RegisterPatientRequest;
import co.edu.ucc.clinic.dto.SpecialtyView;
import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.facade.ClinicFacade;
import co.edu.ucc.clinic.model.Consultation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinica")
public class ClinicController {

    private final ClinicFacade clinicFacade;

    public ClinicController(ClinicFacade clinicFacade) {
        this.clinicFacade = clinicFacade;
    }

    @PostMapping("/paciente")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientProfile registerPatient(@RequestBody RegisterPatientRequest request) {
        return clinicFacade.registerPatient(request);
    }

    @PostMapping("/login")
    public PatientProfile login(@RequestBody LoginRequest request) {
        if (request == null) {
            throw new BusinessException("Debe ingresar documento y contraseña");
        }
        return clinicFacade.login(request.document(), request.password());
    }

    @GetMapping("/paciente/{pacienteId}")
    public PatientProfile getProfile(@PathVariable("pacienteId") Long patientId) {
        return clinicFacade.getProfile(patientId);
    }

    @PostMapping("/cita")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentConfirmation scheduleAppointment(@RequestBody AppointmentRequest request) {
        if (request == null) {
            throw new BusinessException("Los datos de la cita son obligatorios");
        }
        return clinicFacade.scheduleAppointment(request.patientId(), request.specialty(), request.doctorId(), request.dateTime());
    }

    @DeleteMapping("/cita/{citaId}")
    public AppointmentView cancelAppointment(@PathVariable("citaId") Long appointmentId,
                                             @RequestParam("pacienteId") Long patientId) {
        return clinicFacade.cancelAppointment(patientId, appointmentId);
    }

    @GetMapping("/historia/{pacienteId}")
    public FullHistoryResponse getFullHistory(@PathVariable("pacienteId") Long patientId) {
        return clinicFacade.getFullHistory(patientId);
    }

    @PostMapping("/historia/{pacienteId}/alergias")
    public List<String> addAllergy(@PathVariable("pacienteId") Long patientId, @RequestBody AllergyRequest request) {
        return clinicFacade.addAllergy(patientId, request == null ? null : request.allergy());
    }

    @PostMapping("/consulta")
    @ResponseStatus(HttpStatus.CREATED)
    public Consultation registerConsultation(@RequestBody ConsultationRequest request) {
        return clinicFacade.registerConsultation(request);
    }

    @PostMapping("/prescripcion")
    @ResponseStatus(HttpStatus.CREATED)
    public PrescriptionResponse generatePrescription(@RequestBody PrescriptionRequest request) {
        if (request == null) {
            throw new BusinessException("Los datos de la prescripción son obligatorios");
        }
        return clinicFacade.generatePrescription(request.patientId(), request.medications(), request.instructions());
    }

    @PostMapping("/laboratorio")
    @ResponseStatus(HttpStatus.CREATED)
    public LabResponse requestExams(@RequestBody LabRequest request) {
        if (request == null) {
            throw new BusinessException("Los datos de la solicitud son obligatorios");
        }
        return clinicFacade.requestExams(request.patientId(), request.exams());
    }

    @GetMapping("/medicos")
    public List<DoctorAvailability> getDoctors(@RequestParam(value = "especialidad", required = false) String specialty) {
        return clinicFacade.getDoctors(specialty);
    }

    @GetMapping("/especialidades")
    public List<SpecialtyView> getSpecialties() {
        return clinicFacade.getSpecialties();
    }

    @GetMapping("/examenes")
    public List<Map<String, String>> getExams() {
        return clinicFacade.getAvailableExams();
    }
}
