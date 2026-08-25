import type { ReactNode } from 'react'
import { AlertIcon, CheckIcon } from './Icons'

export function Alert({ kind = 'error', children }: { kind?: 'error' | 'success' | 'info'; children: ReactNode }) {
  return (
    <div className={`alert alert--${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      {kind === 'success' ? <CheckIcon /> : <AlertIcon />}
      <div>{children}</div>
    </div>
  )
}
