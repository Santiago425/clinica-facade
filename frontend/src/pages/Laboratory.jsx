import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import Alert from '../components/Alert.jsx'
import LabResultCard from '../components/LabResultCard.jsx'

export default function Laboratory() {
  const { patient } = useAuth()
  const [exams, setExams] = useState([])
  const [selected, setSelected] = useState([])
  const [response, setResponse] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.exams().then(setExams).catch((err) => setError(err.message))
  }, [])

  const toggle = (code) => {
    setSelected(selected.includes(code) ? selected.filter((c) => c !== code) : [...selected, code])
  }

  const handleRequest = async () => {
    setError('')
    setLoading(true)
    try {
      setResponse(await api.laboratory({ patientId: patient.id, exams: selected }))
      setSelected([])
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
          <p className="eyebrow">Laboratorio clínico</p>
          <h1>Solicitar exámenes</h1>
          <p className="muted">Seleccione los exámenes. Los resultados se generan con sus valores de referencia.</p>
        </div>
      </header>

      <section className="card">
        <div className="options">
          {exams.map((exam) => (
            <button key={exam.code} className={`option ${selected.includes(exam.code) ? 'selected' : ''}`} onClick={() => toggle(exam.code)}>
              {selected.includes(exam.code) ? '✓ ' : ''}{exam.name}
            </button>
          ))}
        </div>
        <Alert>{error}</Alert>
        <button className="button primary" onClick={handleRequest} disabled={loading || selected.length === 0}>
          {loading ? 'Procesando…' : 'Solicitar exámenes'}
        </button>
      </section>

      {response && (
        <>
          <Alert type={response.outOfRangeCount ? 'error' : 'success'}>
            {response.outOfRangeCount
              ? `Se encontraron ${response.outOfRangeCount} valores fuera del rango normal. Consulte con su médico.`
              : 'Todos los valores están dentro del rango normal.'}
          </Alert>
          <div className="grid-cards">
            {response.results.map((result) => <LabResultCard key={result.id} result={result} />)}
          </div>
        </>
      )}
    </div>
  )
}
