import { useEffect, useMemo, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { dayKey, formatDateTime, formatDay, formatTime } from '../format.js'
import Alert from '../components/Alert.jsx'

export default function Schedule() {
  const { patient } = useAuth()
  const [specialties, setSpecialties] = useState([])
  const [specialty, setSpecialty] = useState('')
  const [doctors, setDoctors] = useState([])
  const [doctorId, setDoctorId] = useState(null)
  const [day, setDay] = useState('')
  const [slot, setSlot] = useState('')
  const [confirmation, setConfirmation] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.specialties().then(setSpecialties).catch((err) => setError(err.message))
  }, [])

  const loadDoctors = (code) => {
    setSpecialty(code)
    setDoctorId(null)
    setDay('')
    setSlot('')
    setConfirmation(null)
    api.doctors(code).then(setDoctors).catch((err) => setError(err.message))
  }

  const doctor = doctors.find((d) => d.id === doctorId)

  const slotsByDay = useMemo(() => {
    if (!doctor) return {}
    return doctor.availableSlots.reduce((acc, value) => {
      const key = dayKey(value)
      acc[key] = [...(acc[key] || []), value]
      return acc
    }, {})
  }, [doctor])

  const handleConfirm = async () => {
    setError('')
    setLoading(true)
    try {
      const result = await api.schedule({ patientId: patient.id, specialty, doctorId, dateTime: slot })
      setConfirmation(result)
      setSlot('')
      setDay('')
      setDoctors(await api.doctors(specialty))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <p className="eyebrow">Agenda médica</p>
          <h1>Agendar nueva cita</h1>
        </div>
      </header>

      {confirmation && (
        <section className="card confirmation">
          <h2>¡Cita agendada!</h2>
          <p>
            <strong>{confirmation.appointment.specialtyName}</strong> con {confirmation.appointment.doctorName} el{' '}
            {formatDateTime(confirmation.appointment.dateTime)}.
          </p>
          <h3>Recordatorios</h3>
          <ul>
            {confirmation.reminders.map((reminder) => <li key={reminder}>{reminder}</li>)}
          </ul>
        </section>
      )}

      <Alert>{error}</Alert>

      <section className="step">
        <h2 className="section-title"><span className="step-number">1</span>Especialidad</h2>
        <div className="options">
          {specialties.map((s) => (
            <button key={s.code} className={`option ${specialty === s.code ? 'selected' : ''}`} onClick={() => loadDoctors(s.code)}>
              {s.name}
            </button>
          ))}
        </div>
      </section>

      {specialty && (
        <section className="step">
          <h2 className="section-title"><span className="step-number">2</span>Médico</h2>
          <div className="options">
            {doctors.map((d) => (
              <button key={d.id} className={`option doctor ${doctorId === d.id ? 'selected' : ''}`} onClick={() => { setDoctorId(d.id); setDay(''); setSlot('') }}>
                <strong>{d.name}</strong>
                <small>{d.office} · {d.availableSlots.length} horarios libres</small>
              </button>
            ))}
          </div>
        </section>
      )}

      {doctor && (
        <section className="step">
          <h2 className="section-title"><span className="step-number">3</span>Fecha y hora</h2>
          <div className="days">
            {Object.keys(slotsByDay).map((key) => (
              <button key={key} className={`day ${day === key ? 'selected' : ''}`} onClick={() => { setDay(key); setSlot('') }}>
                {formatDay(slotsByDay[key][0])}
              </button>
            ))}
          </div>
          {day && (
            <div className="times">
              {slotsByDay[day].map((value) => (
                <button key={value} className={`time ${slot === value ? 'selected' : ''}`} onClick={() => setSlot(value)}>
                  {formatTime(value)}
                </button>
              ))}
            </div>
          )}
        </section>
      )}

      {slot && (
        <div className="confirm-bar">
          <span>{doctor.name} · {formatDateTime(slot)}</span>
          <button className="button primary" onClick={handleConfirm} disabled={loading}>
            {loading ? 'Agendando…' : 'Confirmar cita'}
          </button>
        </div>
      )}
    </div>
  )
}
