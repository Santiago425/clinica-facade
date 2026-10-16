const locale = 'es-CO'

export function formatDate(value) {
  return new Date(value).toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' })
}

export function formatDateTime(value) {
  return new Date(value).toLocaleString(locale, {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

export function formatDay(value) {
  return new Date(value).toLocaleDateString(locale, { weekday: 'short', day: 'numeric', month: 'short' })
}

export function formatTime(value) {
  return new Date(value).toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit', hour12: false })
}

export function dayKey(value) {
  return value.slice(0, 10)
}

export const statusLabels = {
  SCHEDULED: 'Agendada',
  CANCELLED: 'Cancelada',
  ATTENDED: 'Atendida',
}
