const BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${BASE_URL}/api/clinica${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    })
  } catch {
    throw new Error('No fue posible conectar con el servidor. Intente nuevamente.')
  }
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(data?.message || 'Ocurrió un error inesperado')
  }
  return data
}

export const api = {
  register: (body) => request('/paciente', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) => request('/login', { method: 'POST', body: JSON.stringify(body) }),
  profile: (patientId) => request(`/paciente/${patientId}`),
  specialties: () => request('/especialidades'),
  doctors: (specialty) => request(`/medicos?especialidad=${encodeURIComponent(specialty)}`),
  schedule: (body) => request('/cita', { method: 'POST', body: JSON.stringify(body) }),
  cancel: (appointmentId, patientId) => request(`/cita/${appointmentId}?pacienteId=${patientId}`, { method: 'DELETE' }),
  history: (patientId) => request(`/historia/${patientId}`),
  addAllergy: (patientId, allergy) => request(`/historia/${patientId}/alergias`, { method: 'POST', body: JSON.stringify({ allergy }) }),
  consultation: (body) => request('/consulta', { method: 'POST', body: JSON.stringify(body) }),
  prescription: (body) => request('/prescripcion', { method: 'POST', body: JSON.stringify(body) }),
  exams: () => request('/examenes'),
  laboratory: (body) => request('/laboratorio', { method: 'POST', body: JSON.stringify(body) }),
}
