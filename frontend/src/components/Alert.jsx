export default function Alert({ type = 'error', children }) {
  if (!children) return null
  return <div className={`alert ${type}`}>{children}</div>
}
