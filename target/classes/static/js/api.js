async function req(path, opts) {
  const res = await fetch(path, opts);
  let body = null;
  try { body = await res.json(); } catch (e) { /* empty body */ }
  if (!res.ok) {
    const msg = (body && (body.message || body.error || JSON.stringify(body))) || ('HTTP ' + res.status);
    throw new Error(msg);
  }
  return body;
}
const get = (p) => req(p);
const post = (p, data) => req(p, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: data ? JSON.stringify(data) : undefined,
});

export const api = {
  nodes: () => get('/api/nodes'),
  roads: () => get('/api/roads'),
  ambulances: () => get('/api/ambulances'),
  hospitals: () => get('/api/hospitals'),
  emergencies: () => get('/api/emergencies'),
  dispatches: () => get('/api/dispatches').catch(() => []),
  summary: () => get('/api/reports/summary').catch(() => null),
  createEmergency: (d) => post('/api/emergencies', d),
  reset: () => post('/api/simulation/reset'),
};
