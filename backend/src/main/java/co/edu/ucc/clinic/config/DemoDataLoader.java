package co.edu.ucc.clinic.config;

import co.edu.ucc.clinic.dto.DoctorAvailability;
import co.edu.ucc.clinic.dto.PatientProfile;
import co.edu.ucc.clinic.dto.RegisterPatientRequest;
import co.edu.ucc.clinic.facade.ClinicFacade;
import co.edu.ucc.clinic.model.Appointment;
import co.edu.ucc.clinic.model.ExamType;
import co.edu.ucc.clinic.model.Medication;
import co.edu.ucc.clinic.service.LaboratoryService;
import co.edu.ucc.clinic.service.MedicalHistoryService;
import co.edu.ucc.clinic.service.PrescriptionService;
import co.edu.ucc.clinic.service.ScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DemoDataLoader implements CommandLineRunner {

    private final ClinicFacade clinicFacade;
    private final ScheduleService scheduleService;
    private final MedicalHistoryService medicalHistoryService;
    private final PrescriptionService prescriptionService;
    private final LaboratoryService laboratoryService;

    public DemoDataLoader(ClinicFacade clinicFacade,
                          ScheduleService scheduleService,
                          MedicalHistoryService medicalHistoryService,
                          PrescriptionService prescriptionService,
                          LaboratoryService laboratoryService) {
        this.clinicFacade = clinicFacade;
        this.scheduleService = scheduleService;
        this.medicalHistoryService = medicalHistoryService;
        this.prescriptionService = prescriptionService;
        this.laboratoryService = laboratoryService;
    }

    @Override
    public void run(String... args) {
        PatientProfile patient = clinicFacade.registerPatient(new RegisterPatientRequest(
                "1085123456",
                "Valentina",
                "Ortiz",
                "valentina.ortiz@correo.com",
                "3104567890",
                LocalDate.of(1998, 5, 14),
                "demo123",
                List.of("Penicilina")
        ));
        Long patientId = patient.id();
        LocalDateTime today = ClinicClock.today().atTime(9, 0);

        Appointment general = scheduleService.registerPastAppointment(patientId, 1L, today.minusDays(45));
        medicalHistoryService.registerConsultation(patientId, general.getId(), general.getDateTime(),
                general.getDoctor().name(), general.getDoctor().specialty().getLabel(),
                "Dolor de garganta y fiebre", "Faringitis aguda",
                "Se indica reposo, hidratación y control en caso de persistir la fiebre.");
        prescriptionService.registerPast(patientId, general.getDateTime(), List.of(
                new Medication("Azitromicina 500 mg", "1 tableta", "Cada 24 horas", "3 días"),
                new Medication("Acetaminofén 500 mg", "1 tableta", "Cada 8 horas", "5 días")
        ), "Tomar después de las comidas.");
        laboratoryService.registerPast(patientId, ExamType.HEMOGRAM, general.getDateTime().plusDays(1),
                13.8, 41.2, 12.6, 280.0);

        Appointment cardiology = scheduleService.registerPastAppointment(patientId, 3L, today.minusDays(20).withHour(15));
        medicalHistoryService.registerConsultation(patientId, cardiology.getId(), cardiology.getDateTime(),
                cardiology.getDoctor().name(), cardiology.getDoctor().specialty().getLabel(),
                "Chequeo cardiovascular preventivo", "Dislipidemia leve",
                "Se recomienda dieta baja en grasas, actividad física y control de perfil lipídico.");
        prescriptionService.registerPast(patientId, cardiology.getDateTime(), List.of(
                new Medication("Atorvastatina 20 mg", "1 tableta", "Cada noche", "90 días")
        ), "Control con resultados de laboratorio en 3 meses.");
        laboratoryService.registerPast(patientId, ExamType.LIPID_PROFILE, cardiology.getDateTime().minusDays(2),
                228.0, 46.0, 152.0, 171.0);
        laboratoryService.registerPast(patientId, ExamType.GLYCEMIA, cardiology.getDateTime().minusDays(2),
                92.0);

        List<DoctorAvailability> dermatology = clinicFacade.getDoctors("DERMATOLOGY");
        if (!dermatology.isEmpty() && dermatology.get(0).availableSlots().size() > 3) {
            clinicFacade.scheduleAppointment(patientId, "DERMATOLOGY", dermatology.get(0).id(),
                    dermatology.get(0).availableSlots().get(3));
        }
    }
}
