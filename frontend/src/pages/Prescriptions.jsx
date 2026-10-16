import { useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { formatDate } from '../format.js'
import Alert from '../components/Alert.jsx'

const emptyMedication = { name: '', dose: '', frequency: '', duration: '' }

export default function Prescriptions() {
  const { patient } = useAuth()
  const [medications, setMedications] = useState([{ ...emptyMedication }])
  const [instructions, setInstructions] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (index, field, value) => {
    setMedications(medications.map((m, i) => (i === index ? { ...m, [field]: value } : m)))
  }

  const remove = (index) => setMedications(medications.filter((_, i) => i !== index))

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setResult(null)
    setLoading(true)
    try {
      const response = await api.prescription({ patientId: patient.id, medications, instructions })
      setResult(response)
      setMedications([{ ...emptyMedication }])
      setInstructions('')
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
          <p className="eyebrow">Prescripción médica</p>
          <h1>Generar prescripción</h1>
          <p className="muted">Cada medicamento se valida contra las alergias registradas en su historia clínica.</p>
        </div>
      </header>

      <form className="card" onSubmit={handleSubmit}>
        {medications.map((m, index) => (
          <div key={index} className="med-row">
            <label>Medicamento<input value={m.name} onChange={(e) => update(index, 'name', e.target.value)} placeholder="Ej: Loratadina 10 mg" required /></label>
            <label>Dosis<input value={m.dose} onChange={(e) => update(index, 'dose', e.target.value)} placeholder="1 tableta" required /></label>
            <label>Frecuencia<input value={m.frequency} onChange={(e) => update(index, 'frequency', e.target.value)} placeholder="Cada 8 horas" /></label>
            <label>Duración<input value={m.duration} onChange={(e) => update(index, 'duration', e.target.value)} placeholder="5 días" required /></label>
            {medications.length > 1 && (
              <button type="button" className="icon-button" onClick={() => remove(index)} aria-label="Quitar medicamento">×</button>
            )}
          </div>
        ))}
        <button type="button" className="button ghost small" onClick={() => setMedications([...medications, { ...emptyMedication }])}>
          + Agregar medicamento
        </button>
        <label>
          Indicaciones generales
          <textarea value={instructions} onChange={(e) => setInstructions(e.target.value)} placeholder="Tomar después de las comidas" />
        </label>
        <Alert>{error}</Alert>
        <button className="button primary" disabled={loading}>{loading ? 'Validando…' : 'Generar prescripción'}</button>
      </form>

      {result && (
        <section className="card confirmation">
          <h2>Prescripción {result.prescription.code}</h2>
          <p>Paciente: {result.patientName} · {formatDate(result.prescription.issuedAt)}</p>
          <p className="muted">
            Alergias verificadas: {result.checkedAllergies.length ? result.checkedAllergies.join(', ') : 'ninguna registrada'}
          </p>
          <table className="table">
            <thead>
              <tr><th>Medicamento</th><th>Dosis</th><th>Frecuencia</th><th>Duración</th></tr>
            </thead>
            <tbody>
              {result.prescription.medications.map((m, index) => (
                <tr key={index}><td>{m.name}</td><td>{m.dose}</td><td>{m.frequency}</td><td>{m.duration}</td></tr>
              ))}
            </tbody>
          </table>
          <p>{result.prescription.instructions}</p>
        </section>
      )}
    </div>
  )
}
