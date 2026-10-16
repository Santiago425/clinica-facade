import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { formatDateTime } from '../format.js'
import Alert from '../components/Alert.jsx'
import LabResultCard from '../components/LabResultCard.jsx'

export default function Dashboard() {
  const { patient } = useAuth()
  const [history, setHistory] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const load = () => {
    api.history(patient.id).then(setHistory).catch((err) => setError(err.message))
  }

  useEffect(load, [patient.id])

  const handleCancel = async (appointmentId) => {
    setError('')
    setNotice('')
    try {
      await api.cancel(appointmentId, patient.id)
      setNotice('La cita fue cancelada correctamente.')
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!history) return <div className="loading">{error || 'Cargando su información…'}</div>

  const latestResults = history.labResults.slice(0, 2)
  const outOfRange = history.labResults.flatMap((r) => r.values).filter((v) => !v.withinRange).length

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <p className="eyebrow">Bienvenido(a)</p>
          <h1>Hola, {patient.firstName}</h1>
        </div>
        <Link to="/agendar" className="button primary">Agendar nueva cita</Link>
      </header>

      <section className="stats">
        <div className="stat"><span>{history.upcomingAppointments.length}</span>Próximas citas</div>
        <div className="stat"><span>{history.consultations.length}</span>Consultas registradas</div>
        <div className="stat"><span>{history.prescriptions.length}</span>Prescripciones</div>
        <div className={`stat ${outOfRange ? 'warn' : ''}`}><span>{outOfRange}</span>Valores fuera de rango</div>
      </section>

      {history.allergies.length > 0 && (
        <div className="allergy-strip">
          <strong>Alergias registradas:</strong>
          {history.allergies.map((a) => <span key={a} className="chip danger">{a}</span>)}
        </div>
      )}

      <Alert>{error}</Alert>
      <Alert type="success">{notice}</Alert>

      <div className="two-col">
        <section>
          <h2 className="section-title">Próximas citas</h2>
          {history.upcomingAppointments.length === 0 && (
            <div className="empty">No tiene citas próximas. <Link to="/agendar">Agende una aquí.</Link></div>
          )}
          <div className="stack">
            {history.upcomingAppointments.map((appointment) => (
              <article key={appointment.id} className="card appointment">
                <div className="appointment-date">{formatDateTime(appointment.dateTime)}</div>
                <h3>{appointment.specialtyName}</h3>
                <p>{appointment.doctorName} · {appointment.office}</p>
                <button className="button ghost small" onClick={() => handleCancel(appointment.id)}>Cancelar cita</button>
              </article>
            ))}
          </div>
        </section>
        <section>
          <h2 className="section-title">Últimos resultados</h2>
          {latestResults.length === 0 && (
            <div className="empty">Aún no tiene resultados. <Link to="/laboratorio">Solicite exámenes.</Link></div>
          )}
          <div className="stack">
            {latestResults.map((result) => <LabResultCard key={result.id} result={result} />)}
          </div>
        </section>
      </div>
    </div>
  )
}
