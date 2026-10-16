import RangeIndicator from './RangeIndicator.jsx'
import { formatDate } from '../format.js'

export default function LabResultCard({ result }) {
  const outOfRange = result.values.filter((value) => !value.withinRange).length
  return (
    <article className="card lab-card">
      <header className="card-head">
        <div>
          <h3>{result.examName}</h3>
          <small>{formatDate(result.requestedAt)}</small>
        </div>
        <span className={`badge ${outOfRange ? 'high' : 'normal'}`}>
          {outOfRange ? `${outOfRange} fuera de rango` : 'Todo en rango'}
        </span>
      </header>
      {result.values.map((value) => (
        <RangeIndicator key={value.parameter} item={value} />
      ))}
    </article>
  )
}
