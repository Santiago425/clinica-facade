import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import Layout from './components/Layout.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Schedule from './pages/Schedule.jsx'
import History from './pages/History.jsx'
import Prescriptions from './pages/Prescriptions.jsx'
import Laboratory from './pages/Laboratory.jsx'

function Protected({ children }) {
  const { patient } = useAuth()
  return patient ? children : <Navigate to="/ingresar" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/ingresar" element={<Login />} />
      <Route path="/registro" element={<Register />} />
      <Route
        element={
          <Protected>
            <Layout />
          </Protected>
        }
      >
        <Route path="/" element={<Dashboard />} />
        <Route path="/agendar" element={<Schedule />} />
        <Route path="/historia" element={<History />} />
        <Route path="/prescripciones" element={<Prescriptions />} />
        <Route path="/laboratorio" element={<Laboratory />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
