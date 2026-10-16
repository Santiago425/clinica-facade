# Sistema de Gestión Médica · Patrón Facade

Taller de **Patrones de Software** – Universidad Cooperativa de Colombia.

Portal para pacientes de una clínica donde pueden registrarse, agendar citas, consultar su historia clínica, generar prescripciones médicas y solicitar exámenes de laboratorio. Toda la complejidad interna (validaciones y servicios especializados) queda oculta detrás de una única fachada: `ClinicFacade`.

## Integrantes

| Integrante | GitHub |
|---|---|
| Santiago Campoverde | [@Santiago425](https://github.com/Santiago425) |
| | [@Chindoy12](https://github.com/Chindoy12) |
| | [@laurameza7](https://github.com/laurameza7) |

## Despliegue

- **Frontend (Vercel):** _pendiente_
- **Backend (Railway):** _pendiente_

Usuario de prueba: documento `1085123456`, contraseña `demo123`.

## Patrón Facade

```
            Cliente (Frontend React)
                      │
                ClinicController   (REST /api/clinica)
                      │
                ClinicFacade       ← interfaz única y simplificada
   ┌──────────┬───────┼──────────────┬───────────────┐
PatientService ScheduleService MedicalHistoryService PrescriptionService LaboratoryService
```

| Subsistema | Responsabilidad |
|---|---|
| `PatientService` | Registro con datos personales, validación de documento único, autenticación y consulta de perfil |
| `ScheduleService` | 4 especialidades, 5 médicos predefinidos, disponibilidad, agendar y cancelar citas, recordatorios |
| `MedicalHistoryService` | Registro y consulta de consultas previas, diagnósticos y alergias |
| `PrescriptionService` | Prescripciones con medicamentos, dosis, frecuencia y duración; detección de conflictos con alergias (incluye familias como penicilinas o AINES) |
| `LaboratoryService` | Hemograma, glicemia y perfil lipídico con resultados simulados y valores de referencia |

Métodos de la fachada (cada uno invoca mínimo 2 subsistemas):

| Método | Subsistemas que coordina |
|---|---|
| `scheduleAppointment(patientId, specialty, date)` | Patient + Schedule + MedicalHistory |
| `getFullHistory(patientId)` | Los 5 subsistemas: consolida citas, consultas, prescripciones y laboratorios en un solo objeto |
| `generatePrescription(patientId, medications[])` | Patient + MedicalHistory (alergias) + Prescription |
| `requestExams(patientId, exams[])` | Patient + Laboratory |
| `registerPatient`, `login`, `cancelAppointment`, `registerConsultation`, `addAllergy` | Patient + Schedule / MedicalHistory |

## Endpoints REST

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/clinica/paciente` | Registrar paciente |
| POST | `/api/clinica/login` | Iniciar sesión |
| GET | `/api/clinica/paciente/{pacienteId}` | Perfil del paciente |
| POST | `/api/clinica/cita` | Agendar cita |
| DELETE | `/api/clinica/cita/{citaId}?pacienteId=` | Cancelar cita |
| GET | `/api/clinica/historia/{pacienteId}` | Historia clínica completa |
| POST | `/api/clinica/historia/{pacienteId}/alergias` | Agregar alergia |
| POST | `/api/clinica/consulta` | Registrar consulta de una cita |
| POST | `/api/clinica/prescripcion` | Generar prescripción con validación de alergias |
| POST | `/api/clinica/laboratorio` | Solicitar exámenes |
| GET | `/api/clinica/medicos?especialidad=` | Médicos y horarios disponibles |
| GET | `/api/clinica/especialidades` | Especialidades |
| GET | `/api/clinica/examenes` | Exámenes disponibles |

Los datos se manejan completamente en memoria.

Ejemplo para agendar una cita:

```json
POST /api/clinica/cita
{
  "patientId": 1,
  "specialty": "CARDIOLOGY",
  "doctorId": 3,
  "dateTime": "2026-10-20T09:00:00"
}
```

## Frontend

- Registro e inicio de sesión
- Dashboard con próximas citas y últimos resultados
- Agendar cita por especialidad, médico y horario disponible
- Historia clínica con pestañas: consultas, prescripciones y laboratorios
- Resultados de laboratorio con indicador visual dentro o fuera del rango normal

## Ejecución local

Backend (Java 17 + Maven):

```bash
cd backend
mvn spring-boot:run
```

Frontend (Node 18+):

```bash
cd frontend
npm install
echo VITE_API_URL=http://localhost:8080 > .env
npm run dev
```

## Tecnologías

Spring Boot 3 · Java 17 · React 18 · Vite · React Router · Railway · Vercel
