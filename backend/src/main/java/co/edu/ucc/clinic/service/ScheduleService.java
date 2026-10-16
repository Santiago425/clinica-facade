package co.edu.ucc.clinic.service;

import co.edu.ucc.clinic.config.ClinicClock;
import co.edu.ucc.clinic.dto.DoctorAvailability;
import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.exception.NotFoundException;
import co.edu.ucc.clinic.model.Appointment;
import co.edu.ucc.clinic.model.AppointmentStatus;
import co.edu.ucc.clinic.model.Doctor;
import co.edu.ucc.clinic.model.Specialty;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ScheduleService {

    private static final int DAYS_AHEAD = 14;
    private static final List<LocalTime> WORKING_HOURS = List.of(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
    );
    private static final DateTimeFormatter REMINDER_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", Locale.forLanguageTag("es-CO"));

    private final List<Doctor> doctors = List.of(
            new Doctor(1L, "Dra. Ana María Rosero", Specialty.GENERAL_MEDICINE, "Consultorio 101"),
            new Doctor(2L, "Dr. Carlos Benavides", Specialty.GENERAL_MEDICINE, "Consultorio 102"),
            new Doctor(3L, "Dr. Jorge Eraso", Specialty.CARDIOLOGY, "Consultorio 201"),
            new Doctor(4L, "Dra. Paola Delgado", Specialty.PEDIATRICS, "Consultorio 301"),
            new Doctor(5L, "Dra. Lucía Burbano", Specialty.DERMATOLOGY, "Consultorio 302")
    );
    private final Map<Long, Appointment> appointments = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Specialty> getSpecialties() {
        return Arrays.asList(Specialty.values());
    }

    public Specialty parseSpecialty(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Debe indicar la especialidad");
        }
        return Arrays.stream(Specialty.values())
                .filter(s -> s.name().equalsIgnoreCase(value.trim()) || s.getLabel().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("La especialidad " + value + " no existe"));
    }

    public List<Doctor> getDoctors(Specialty specialty) {
        return doctors.stream()
                .filter(doctor -> specialty == null || doctor.specialty() == specialty)
                .toList();
    }

    public Doctor getDoctor(Long doctorId) {
        return doctors.stream()
                .filter(doctor -> doctor.id().equals(doctorId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe un médico con id " + doctorId));
    }

    public List<DoctorAvailability> getAvailability(Specialty specialty) {
        return getDoctors(specialty).stream()
                .map(doctor -> new DoctorAvailability(
                        doctor.id(),
                        doctor.name(),
                        doctor.specialty().name(),
                        doctor.specialty().getLabel(),
                        doctor.office(),
                        availableSlots(doctor)))
                .toList();
    }

    public synchronized Appointment book(Long patientId, Specialty specialty, Long doctorId, LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new BusinessException("Debe indicar la fecha y hora de la cita");
        }
        LocalDateTime slot = dateTime.truncatedTo(ChronoUnit.MINUTES);
        if (!slot.isAfter(ClinicClock.now())) {
            throw new BusinessException("La cita debe ser en una fecha futura");
        }
        if (!isWorkingSlot(slot)) {
            throw new BusinessException("El horario seleccionado no está dentro de la jornada de atención");
        }
        if (hasActiveAppointmentAt(patientId, slot)) {
            throw new BusinessException("El paciente ya tiene una cita agendada en ese horario");
        }
        if (hasActiveAppointmentSameDay(patientId, specialty, slot.toLocalDate())) {
            throw new BusinessException("El paciente ya tiene una cita de " + specialty.getLabel() + " ese día");
        }
        Doctor doctor = doctorId != null ? getDoctor(doctorId) : firstAvailableDoctor(specialty, slot);
        if (doctor.specialty() != specialty) {
            throw new BusinessException(doctor.name() + " no atiende la especialidad " + specialty.getLabel());
        }
        if (!isDoctorFree(doctor, slot)) {
            throw new BusinessException("El horario ya no está disponible para " + doctor.name());
        }
        Appointment appointment = new Appointment(sequence.getAndIncrement(), patientId, doctor, slot, AppointmentStatus.SCHEDULED);
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    public synchronized Appointment registerPastAppointment(Long patientId, Long doctorId, LocalDateTime dateTime) {
        Appointment appointment = new Appointment(sequence.getAndIncrement(), patientId, getDoctor(doctorId), dateTime, AppointmentStatus.ATTENDED);
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    public synchronized Appointment cancel(Long appointmentId, Long patientId) {
        Appointment appointment = getAppointment(appointmentId);
        if (patientId != null && !appointment.getPatientId().equals(patientId)) {
            throw new BusinessException("La cita no pertenece al paciente");
        }
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Solo se pueden cancelar citas agendadas");
        }
        if (!appointment.getDateTime().isAfter(ClinicClock.now().plusHours(2))) {
            throw new BusinessException("Las citas solo se pueden cancelar con mínimo 2 horas de anticipación");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointment;
    }

    public synchronized Appointment markAttended(Long appointmentId) {
        Appointment appointment = getAppointment(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Solo se pueden atender citas agendadas");
        }
        appointment.setStatus(AppointmentStatus.ATTENDED);
        return appointment;
    }

    public Appointment getAppointment(Long appointmentId) {
        Appointment appointment = appointmentId == null ? null : appointments.get(appointmentId);
        if (appointment == null) {
            throw new NotFoundException("No existe una cita con id " + appointmentId);
        }
        return appointment;
    }

    public List<Appointment> getAppointments(Long patientId) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getPatientId().equals(patientId))
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .toList();
    }

    public List<Appointment> getUpcomingAppointments(Long patientId) {
        LocalDateTime now = ClinicClock.now();
        return getAppointments(patientId).stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(appointment -> appointment.getDateTime().isAfter(now))
                .toList();
    }

    public List<Appointment> getPastAppointments(Long patientId) {
        LocalDateTime now = ClinicClock.now();
        return getAppointments(patientId).stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.ATTENDED
                        || (appointment.getStatus() == AppointmentStatus.SCHEDULED && !appointment.getDateTime().isAfter(now)))
                .sorted(Comparator.comparing(Appointment::getDateTime).reversed())
                .toList();
    }

    public List<String> buildReminders(Appointment appointment) {
        LocalDateTime dateTime = appointment.getDateTime();
        List<String> reminders = new ArrayList<>();
        reminders.add("Su cita es el " + dateTime.format(REMINDER_FORMAT) + " con " + appointment.getDoctor().name() + ".");
        reminders.add("Preséntese en el " + appointment.getDoctor().office() + " 15 minutos antes de la hora asignada.");
        LocalDateTime reminderAt = dateTime.minusDays(1);
        if (reminderAt.isAfter(ClinicClock.now())) {
            reminders.add("Recibirá un recordatorio el " + reminderAt.format(REMINDER_FORMAT) + ".");
        } else {
            reminders.add("Recibirá un recordatorio 2 horas antes de la cita.");
        }
        reminders.add("Traiga su documento de identidad y exámenes recientes si los tiene.");
        reminders.add("Si no puede asistir, cancele la cita con mínimo 2 horas de anticipación.");
        return reminders;
    }

    private List<LocalDateTime> availableSlots(Doctor doctor) {
        LocalDateTime now = ClinicClock.now();
        LocalDate today = ClinicClock.today();
        List<LocalDateTime> slots = new ArrayList<>();
        for (int day = 0; day <= DAYS_AHEAD; day++) {
            LocalDate date = today.plusDays(day);
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            for (LocalTime time : hoursFor(date)) {
                LocalDateTime slot = date.atTime(time);
                if (slot.isAfter(now) && isDoctorFree(doctor, slot)) {
                    slots.add(slot);
                }
            }
        }
        return slots;
    }

    private List<LocalTime> hoursFor(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return WORKING_HOURS.stream().filter(time -> time.isBefore(LocalTime.NOON)).toList();
        }
        return WORKING_HOURS;
    }

    private boolean isWorkingSlot(LocalDateTime slot) {
        LocalDate date = slot.toLocalDate();
        return date.getDayOfWeek() != DayOfWeek.SUNDAY
                && !date.isAfter(ClinicClock.today().plusDays(DAYS_AHEAD))
                && hoursFor(date).contains(slot.toLocalTime());
    }

    private boolean isDoctorFree(Doctor doctor, LocalDateTime slot) {
        return appointments.values().stream()
                .noneMatch(appointment -> appointment.getDoctor().id().equals(doctor.id())
                        && appointment.getDateTime().equals(slot)
                        && appointment.getStatus() != AppointmentStatus.CANCELLED);
    }

    private boolean hasActiveAppointmentAt(Long patientId, LocalDateTime slot) {
        return appointments.values().stream()
                .anyMatch(appointment -> appointment.getPatientId().equals(patientId)
                        && appointment.getDateTime().equals(slot)
                        && appointment.getStatus() == AppointmentStatus.SCHEDULED);
    }

    private boolean hasActiveAppointmentSameDay(Long patientId, Specialty specialty, LocalDate date) {
        return appointments.values().stream()
                .anyMatch(appointment -> appointment.getPatientId().equals(patientId)
                        && appointment.getDoctor().specialty() == specialty
                        && appointment.getDateTime().toLocalDate().equals(date)
                        && appointment.getStatus() == AppointmentStatus.SCHEDULED);
    }

    private Doctor firstAvailableDoctor(Specialty specialty, LocalDateTime slot) {
        return getDoctors(specialty).stream()
                .filter(doctor -> isDoctorFree(doctor, slot))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No hay médicos de " + specialty.getLabel() + " disponibles en ese horario"));
    }
}
