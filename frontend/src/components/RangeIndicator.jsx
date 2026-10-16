export default function RangeIndicator({ item }) {
  const span = item.maxReference - item.minReference
  const lower = item.minReference - span * 0.5
  const upper = item.maxReference + span * 0.5
  const clamp = (value) => Math.min(100, Math.max(0, ((value - lower) / (upper - lower)) * 100))
  const status = item.withinRange ? 'normal' : item.value > item.maxReference ? 'high' : 'low'
  const labels = { normal: 'Normal', high: 'Alto', low: 'Bajo' }

  return (
    <div className={`range-row ${status}`}>
      <div className="range-head">
        <span className="range-name">{item.parameter}</span>
        <span className="range-value">
          {item.value} <small>{item.unit}</small>
        </span>
        <span className={`badge ${status}`}>{labels[status]}</span>
      </div>
      <div className="range-track">
        <div className="range-band" style={{ left: `${clamp(item.minReference)}%`, width: `${clamp(item.maxReference) - clamp(item.minReference)}%` }} />
        <div className="range-marker" style={{ left: `${clamp(item.value)}%` }} />
      </div>
      <div className="range-ref">Referencia: {item.minReference} – {item.maxReference} {item.unit}</div>
    </div>
  )
}
