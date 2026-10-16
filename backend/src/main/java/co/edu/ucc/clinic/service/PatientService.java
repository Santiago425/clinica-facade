package co.edu.ucc.clinic.service;

import co.edu.ucc.clinic.config.ClinicClock;
import co.edu.ucc.clinic.dto.RegisterPatientRequest;
import co.edu.ucc.clinic.exception.BusinessException;
import co.edu.ucc.clinic.exception.NotFoundException;
import co.edu.ucc.clinic.exception.UnauthorizedException;
import co.edu.ucc.clinic.model.Patient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Service
public class PatientService {

    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("\\d{6,12}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final Map<Long, Patient> patients = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public synchronized Patient register(RegisterPatientRequest request) {
        validate(request);
        String document = request.document().trim();
        if (findByDocument(document).isPresent()) {
            throw new BusinessException("Ya existe un paciente registrado con el documento " + document);
        }
        Patient patient = new Patient(
                sequence.getAndIncrement(),
                document,
                request.firstName().trim(),
                request.lastName().trim(),
                request.email().trim().toLowerCase(),
                request.phone() == null ? "" : request.phone().trim(),
                request.birthDate(),
                request.password()
        );
        patients.put(patient.id(), patient);
        return patient;
    }

    public Patient authenticate(String document, String password) {
        if (isBlank(document) || isBlank(password)) {
            throw new BusinessException("Debe ingresar documento y contraseña");
        }
        return findByDocument(document.trim())
                .filter(patient -> patient.password().equals(password))
                .orElseThrow(() -> new UnauthorizedException("Documento o contraseña incorrectos"));
    }

    public Patient getProfile(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("Debe indicar el paciente");
        }
        Patient patient = patients.get(patientId);
        if (patient == null) {
            throw new NotFoundException("No existe un paciente con id " + patientId);
        }
        return patient;
    }

    public Optional<Patient> findByDocument(String document) {
        return patients.values().stream()
                .filter(patient -> patient.document().equals(document))
                .findFirst();
    }

    private void validate(RegisterPatientRequest request) {
        if (request == null) {
            throw new BusinessException("Los datos del paciente son obligatorios");
        }
        if (isBlank(request.document()) || !DOCUMENT_PATTERN.matcher(request.document().trim()).matches()) {
            throw new BusinessException("El documento debe tener entre 6 y 12 dígitos");
        }
        if (isBlank(request.firstName()) || isBlank(request.lastName())) {
            throw new BusinessException("Nombres y apellidos son obligatorios");
        }
        if (isBlank(request.email()) || !EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new BusinessException("El correo electrónico no es válido");
        }
        if (request.birthDate() == null || request.birthDate().isAfter(ClinicClock.today())) {
            throw new BusinessException("La fecha de nacimiento no es válida");
        }
        if (isBlank(request.password()) || request.password().length() < 6) {
            throw new BusinessException("La contraseña debe tener mínimo 6 caracteres");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
