import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { formatDate, formatDateTime, statusLabels } from '../format.js'
import Alert from '../components/Alert.jsx'
import LabResultCard from '../components/LabResultCard.jsx'

const tabs = [
  { id: 'consultations', label: 'Consultas' },
  { id: 'prescriptions', label: 'Prescripciones' },
  { id: 'labs', label: 'Laboratorios' },
]

export default function History() {
  const { patient } = useAuth()
  const [history, setHistory] = useState(null)
  const [tab, setTab] = useState('consultations')
  const [allergy, setAllergy] = useState('')
  const [attending, setAttending] = useState(null)
  const [consultation, setConsultation] = useState({ reason: '', diagnosis: '', notes: '' })
  const [error, setError] = useState('')

  const load = () => api.history(patient.id).then(setHistory).catch((err) => setError(err.message))

  useEffect(() => {
    load()
  }, [patient.id])

  const handleAllergy = async (event) => {
    event.preventDefault()
    setError('')
    try {
      await api.addAllergy(patient.id, allergy)
      setAllergy('')
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleConsultation = async (event) => {
    event.preventDefault()
    setError('')
    try {
      await api.consultation({ appointmentId: attending, ...consultation })
      setAttending(null)
      setConsultation({ reason: '', diagnosis: '', notes: '' })
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!history) return <div className="loading">{error || 'Cargando historia clínica…'}</div>

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <p className="eyebrow">Historia clínica completa</p>
          <h1>{history.patient.firstName} {history.patient.lastName}</h1>
          <p className="muted">CC {history.patient.document} · Nacimiento {formatDate(history.patient.birthDate)} · {history.patient.email}</p>
        </div>
      </header>

      <section className="card summary">
        <div>
          <h3>Alergias</h3>
          <div className="chips">
            {history.allergies.length === 0 && <span className="muted">Sin alergias registradas</span>}
            {history.allergies.map((a) => <span key={a} className="chip danger">{a}</span>)}
          </div>
          <form className="inline-form" onSubmit={handleAllergy}>
            <input value={allergy} onChange={(e) => setAllergy(e.target.value)} placeholder="Agregar alergia" required />
            <button className="button small">Agregar</button>
          </form>
        </div>
        <div>
          <h3>Diagnósticos</h3>
          <div className="chips">
            {history.diagnoses.length === 0 && <span className="muted">Sin diagnósticos</span>}
            {history.diagnoses.map((d) => <span key={d} className="chip">{d}</span>)}
          </div>
        </div>
      </section>

      <Alert>{error}</Alert>

      <div className="tabs">
        {tabs.map((t) => (
          <button key={t.id} className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'consultations' && (
        <div className="stack">
          {history.upcomingAppointments.length > 0 && (
            <section className="card">
              <h3>Citas pendientes por atender</h3>
              {history.upcomingAppointments.map((a) => (
                <div key={a.id} className="list-row">
                  <div>
                    <strong>{a.specialtyName}</strong> · {a.doctorName}
                    <small>{formatDateTime(a.dateTime)}</small>
                  </div>
                  <button className="button ghost small" onClick={() => setAttending(a.id)}>Registrar consulta</button>
                </div>
              ))}
              {attending && (
                <form className="consult-form" onSubmit={handleConsultation}>
                  <label>Motivo<input value={consultation.reason} onChange={(e) => setConsultation({ ...consultation, reason: e.target.value })} /></label>
                  <label>Diagnóstico<input value={consultation.diagnosis} onChange={(e) => setConsultation({ ...consultation, diagnosis: e.target.value })} required /></label>
                  <label>Observaciones<textarea value={consultation.notes} onChange={(e) => setConsultation({ ...consultation, notes: e.target.value })} /></label>
                  <div className="row-actions">
                    <button className="button primary small">Guardar consulta</button>
                    <button type="button" className="button ghost small" onClick={() => setAttending(null)}>Cancelar</button>
                  </div>
                </form>
              )}
            </section>
          )}
          {history.consultations.length === 0 && <div className="empty">No hay consultas registradas.</div>}
          {history.consultations.map((c) => (
            <article key={c.id} className="card timeline-item">
              <div className="timeline-date">{formatDate(c.date)}</div>
              <div>
                <h3>{c.diagnosis}</h3>
                <p className="muted">{c.specialty} · {c.doctorName}</p>
                <p><strong>Motivo:</strong> {c.reason}</p>
                {c.notes && <p><strong>Observaciones:</strong> {c.notes}</p>}
              </div>
            </article>
          ))}
          {history.pastAppointments.length > 0 && (
            <section className="card">
              <h3>Citas pasadas</h3>
              {history.pastAppointments.map((a) => (
                <div key={a.id} className="list-row">
                  <div>
                    <strong>{a.specialtyName}</strong> · {a.doctorName}
                    <small>{formatDateTime(a.dateTime)}</small>
                  </div>
                  <span className="chip">{statusLabels[a.status]}</span>
                </div>
              ))}
            </section>
          )}
        </div>
      )}

      {tab === 'prescriptions' && (
        <div className="stack">
          {history.prescriptions.length === 0 && <div className="empty">No hay prescripciones registradas.</div>}
          {history.prescriptions.map((p) => (
            <article key={p.id} className="card">
              <header className="card-head">
                <div>
                  <h3>{p.code}</h3>
                  <small>{formatDate(p.issuedAt)}</small>
                </div>
              </header>
              <table className="table">
                <thead>
                  <tr><th>Medicamento</th><th>Dosis</th><th>Frecuencia</th><th>Duración</th></tr>
                </thead>
                <tbody>
                  {p.medications.map((m, index) => (
                    <tr key={index}><td>{m.name}</td><td>{m.dose}</td><td>{m.frequency}</td><td>{m.duration}</td></tr>
                  ))}
                </tbody>
              </table>
              <p className="muted">{p.instructions}</p>
            </article>
          ))}
        </div>
      )}

      {tab === 'labs' && (
        <div className="grid-cards">
          {history.labResults.length === 0 && <div className="empty">No hay resultados de laboratorio.</div>}
          {history.labResults.map((r) => <LabResultCard key={r.id} result={r} />)}
        </div>
      )}
    </div>
  )
}
