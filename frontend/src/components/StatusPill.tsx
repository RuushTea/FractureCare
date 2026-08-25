import { label } from '../lib/format'

export function StatusPill({ value }: { value?: string }) {
  const tone = value === 'HIGH_RISK' || value === 'FAILED' ? 'danger'
    : value === 'LOW_RISK' || value === 'PROCESSING' ? 'warning'
      : 'success'
  return <span className={`status-pill status-pill--${tone}`}>{label(value)}</span>
}
