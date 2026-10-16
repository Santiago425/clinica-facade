import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

const links = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/agendar', label: 'Agendar cita' },
  { to: '/historia', label: 'Historia clínica' },
  { to: '/prescripciones', label: 'Prescripciones' },
  { to: '/laboratorio', label: 'Laboratorio' },
]

export default function Layout() {
  const { patient, signOut } = useAuth()
  const navigate = useNavigate()

  const handleSignOut = () => {
    signOut()
    navigate('/ingresar')
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">+</span>
          <div>
            <strong>Clínica Andina</strong>
            <small>Portal del paciente</small>
          </div>
        </div>
        <nav className="nav">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.end} className={({ isActive }) => (isActive ? 'active' : '')}>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="avatar">{patient.firstName[0]}{patient.lastName[0]}</div>
          <div className="who">
            <strong>{patient.firstName} {patient.lastName}</strong>
            <small>CC {patient.document}</small>
          </div>
          <button className="link-button" onClick={handleSignOut}>Salir</button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
