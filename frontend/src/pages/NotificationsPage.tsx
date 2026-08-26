import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { AppShell } from '../components/AppShell'
import { api } from '../lib/api'
import { formatDate } from '../lib/format'
import { navigate } from '../lib/route'
import type { Notification } from '../types'
export function NotificationsPage() { const { token } = useAuth(); const [items, setItems] = useState<Notification[]>([]); useEffect(() => { if (token) api.notifications(token).then(setItems) }, [token]); async function open(item: Notification) { if (!token) return; if (!item.read) { const updated = await api.markNotificationRead(item.id, token); setItems(current => current.map(n => n.id === updated.id ? updated : n)) } if (item.predictionId) navigate(`/results/${item.predictionId}`) } return <AppShell active="notifications"><section className="page-heading"><div><span className="eyebrow">Account updates</span><h1>Notifications</h1><p>Updates about your FractureCare reviews.</p></div></section><section className="history-card">{items.length === 0 ? <div className="empty-state"><h2>No notifications</h2><p>You are all caught up.</p></div> : <div className="history-list">{items.map(item => <button key={item.id} className="history-row" onClick={() => open(item)}><span className="history-ref">{item.read ? 'Read' : 'New'}</span><div><strong>{item.title}</strong><span>{item.message}</span></div><span>{formatDate(item.createdAt)}</span></button>)}</div>}</section></AppShell> }
