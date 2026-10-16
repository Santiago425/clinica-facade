import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import Alert from '../components/Alert.jsx'

const emptyForm = {
  document: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  birthDate: '',
  password: '',
  confirmPassword: '',
  allergies: '',
}

export default function Register() {
  const { signIn } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value })

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Las contraseñas no coinciden')
      return
    }
    setLoading(true)
    try {
      const { confirmPassword, allergies, ...data } = form
      const profile = await api.register({
        ...data,
        allergies: allergies.split(',').map((a) => a.trim()).filter(Boolean),
      })
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
        <h1>Cree su cuenta</h1>
        <p>Con su registro abrimos su historia clínica digital. Indique sus alergias para que sean validadas en cada prescripción.</p>
      </section>
      <section className="auth-panel">
        <form className="auth-form wide" onSubmit={handleSubmit}>
          <h2>Registro de paciente</h2>
          <div className="grid-2">
            <label>Número de documento<input value={form.document} onChange={update('document')} inputMode="numeric" required /></label>
            <label>Fecha de nacimiento<input type="date" value={form.birthDate} onChange={update('birthDate')} required /></label>
            <label>Nombres<input value={form.firstName} onChange={update('firstName')} required /></label>
            <label>Apellidos<input value={form.lastName} onChange={update('lastName')} required /></label>
            <label>Correo electrónico<input type="email" value={form.email} onChange={update('email')} required /></label>
            <label>Teléfono<input value={form.phone} onChange={update('phone')} inputMode="tel" /></label>
            <label>Contraseña<input type="password" value={form.password} onChange={update('password')} required minLength={6} /></label>
            <label>Confirmar contraseña<input type="password" value={form.confirmPassword} onChange={update('confirmPassword')} required /></label>
          </div>
          <label>
            Alergias (separadas por coma)
            <input value={form.allergies} onChange={update('allergies')} placeholder="Ej: Penicilina, Ibuprofeno" />
          </label>
          <Alert>{error}</Alert>
          <button className="button primary" disabled={loading}>{loading ? 'Registrando…' : 'Crear cuenta'}</button>
          <p className="muted center">
            ¿Ya tiene cuenta? <Link to="/ingresar">Inicie sesión</Link>
          </p>
        </form>
      </section>
    </div>
  )
}


