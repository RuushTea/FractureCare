export function label(value?: string) {
  if (!value) return 'Not available'
  return value.toLowerCase().replaceAll('_', ' ').replace(/^./, character => character.toUpperCase())
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export function percentage(value?: number) {
  return value === undefined ? '—' : `${(value * 100).toFixed(1)}%`
}
