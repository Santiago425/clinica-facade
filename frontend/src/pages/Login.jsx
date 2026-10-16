import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import Alert from '../components/Alert.jsx'

export default function Login() {
  const { patient, signIn } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ document: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (patient) return <Navigate to="/" replace />

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const profile = await api.login(form)
      signIn(profile)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-aside">
        <span className="brand-mark big">+</span>
        <h1>Clínica Andina</h1>
        <p>Agende sus citas, consulte su historia clínica y revise sus resultados de laboratorio en un solo lugar.</p>
        <div className="demo-box">
          <strong>Usuario de prueba</strong>
          <span>Documento: 1085123456</span>
          <span>Contraseña: demo123</span>
        </div>
      </section>
      <section className="auth-panel">
        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Iniciar sesión</h2>
          <p className="muted">Ingrese con su documento de identidad.</p>
          <label>
            Número de documento
            <input value={form.document} onChange={(e) => setForm({ ...form, document: e.target.value })} inputMode="numeric" required />
          </label>
          <label>
            Contraseña
            <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          </label>
          <Alert>{error}</Alert>
          <button className="button primary" disabled={loading}>{loading ? 'Ingresando…' : 'Ingresar'}</button>
          <p className="muted center">
            ¿No tiene cuenta? <Link to="/registro">Regístrese aquí</Link>
          </p>
        </form>
      </section>
    </div>
  )
}
