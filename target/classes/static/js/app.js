import { api } from './api.js';
import { initMap, updateAmbulances, updateEmergencies } from './map.js';

const $ = (s) => document.querySelector(s);
const state = { nodes: [], hospitals: [], ambulances: [], emergencies: [], dispatches: [] };
let chart;

const esc = (s) => String(s ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const sevClass = (s) => s >= 8 ? 'crit' : s >= 4 ? 'busy' : 'ok';

function toast(msg, err = false) {
  const t = document.createElement('div');
  t.className = 'toast' + (err ? ' err' : '');
  t.textContent = msg;
  $('#toasts').appendChild(t);
  setTimeout(() => t.remove(), 6000);
}

function renderAmbulances() {
  $('#amb-list').innerHTML = state.ambulances.map(a => {
    const busy = a.status !== 'AVAILABLE';
    const node = state.nodes.find(n => n.id === a.currentNodeId);
    return `<div class="card"><div class="row"><b>${esc(a.callSign)}</b>
      <span class="chip ${busy ? 'busy' : 'AVAILABLE'}">${esc(a.status)}</span></div>
      <div class="muted">${esc(a.type)} · near ${esc(node ? node.name : a.currentNodeId)}</div>
      <div class="bar"><i style="width:${Math.round((a.progress || 0) * 100)}%"></i></div></div>`;
  }).join('') || '<p class="muted">No ambulances.</p>';
}

function renderHospitals() {
  $('#hos-list').innerHTML = state.hospitals.map(h => {
    const pct = h.totalBeds ? Math.round(100 * h.availableBeds / h.totalBeds) : 0;
    return `<div class="card"><div class="row"><b>${esc(h.name)}</b></div>
      <div class="muted">${h.availableBeds} / ${h.totalBeds} beds free</div>
      <div class="bar hos ${pct < 10 ? 'low' : ''}"><i style="width:${pct}%"></i></div>
      <div class="tags">${(h.facilities || []).map(f => `<span>${esc(f)}</span>`).join('')}</div></div>`;
  }).join('');
}

function renderLog() {
  const aN = (id) => (state.ambulances.find(a => a.id === id) || {}).callSign || '—';
  const hN = (id) => (state.hospitals.find(h => h.id === id) || {}).name || '—';
  const rows = [...state.emergencies].sort((a, b) => b.id - a.id);
  $('#log-body').innerHTML = rows.map(e => `<tr><td>${e.id}</td><td>${esc(e.type)}</td>
    <td><span class="chip ${sevClass(e.severity)}">${e.severity}</span></td><td>${esc(e.status)}</td>
    <td>${esc(aN(e.ambulanceId))}</td><td>${esc(hN(e.hospitalId))}</td>
    <td>${e.estimatedMinutes != null ? Number(e.estimatedMinutes).toFixed(1) : '—'}</td></tr>`).join('')
    || '<tr><td colspan="7" class="muted">No emergencies yet. Report one on the left.</td></tr>';
}

async function renderReports() {
  const s = await api.summary();
  const em = state.emergencies;
  const done = em.filter(e => e.status === 'COMPLETED').length;
  const pending = em.filter(e => e.status === 'PENDING').length;
  const etas = em.filter(e => e.estimatedMinutes != null);
  const avg = etas.length ? etas.reduce((t, e) => t + Number(e.estimatedMinutes), 0) / etas.length : 0;
  const kpis = [['Total emergencies', em.length], ['Completed', done], ['Pending', pending], ['Avg ETA (min)', avg.toFixed(1)]];
  $('#kpis').innerHTML = kpis.map(([k, v]) => `<div class="kpi"><span class="muted">${k}</span><b>${v}</b></div>`).join('');
  if (window.Chart) {
    const labels = etas.map(e => '#' + e.id), data = etas.map(e => Number(e.estimatedMinutes));
    if (!chart) {
      chart = new Chart($('#chart'), { type: 'bar',
        data: { labels, datasets: [{ label: 'Estimated response (min)', data, backgroundColor: '#3B82F6' }] },
        options: { plugins: { legend: { labels: { color: '#94A3B8' } } },
          scales: { x: { ticks: { color: '#94A3B8' } }, y: { ticks: { color: '#94A3B8' }, beginAtZero: true } } } });
    } else { chart.data.labels = labels; chart.data.datasets[0].data = data; chart.update(); }
  }
}

async function refresh() {
  try {
    const [amb, hos, em] = await Promise.all([api.ambulances(), api.hospitals(), api.emergencies()]);
    Object.assign(state, { ambulances: amb, hospitals: hos, emergencies: em });
    updateAmbulances(amb);
    updateEmergencies(em, state.nodes, amb, hos);
    renderAmbulances(); renderHospitals(); renderLog();
    if (!$('#tab-reports').hidden) renderReports();
  } catch (e) { console.error(e); }
}

async function dispatch(type, severity, locationNodeId) {
  try {
    const r = await api.createEmergency({ type, severity: Number(severity), locationNodeId: Number(locationNodeId) });
    const amb = state.ambulances.find(a => a.id === r.ambulanceId);
    const hos = state.hospitals.find(h => h.id === r.hospitalId);
    const parts = [`Emergency #${r.id ?? ''} ${r.status ?? 'created'}`];
    if (amb) parts.push('Ambulance ' + amb.callSign);
    if (hos) parts.push(hos.name);
    if (r.estimatedMinutes != null) parts.push('ETA ' + Number(r.estimatedMinutes).toFixed(1) + ' min');
    toast(parts.join(' · '));
  } catch (e) { toast('Dispatch failed: ' + e.message, true); }
  refresh();
}

function randNode() { return state.nodes[Math.floor(Math.random() * state.nodes.length)].id; }

async function scenario(kind) {
  if (kind === 'cardiac') await dispatch('CARDIAC', 9, randNode());
  if (kind === 'minor') await dispatch('MINOR_INJURY', 2, randNode());
  if (kind === 'multi') {
    await dispatch('ACCIDENT', 10, randNode());
    await dispatch('ACCIDENT', 8, randNode());
    await dispatch('TRAUMA', 6, randNode());
  }
}

function exportCsv() {
  const head = ['id', 'type', 'severity', 'status', 'ambulanceId', 'hospitalId', 'estimatedMinutes'];
  const lines = [head.join(',')].concat(state.emergencies.map(e => head.map(h => JSON.stringify(e[h] ?? '')).join(',')));
  const a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob([lines.join('\n')], { type: 'text/csv' }));
  a.download = 'lifegrid-dispatch-log.csv'; a.click();
}

async function init() {
  try {
    const [nodes, roads, hospitals] = await Promise.all([api.nodes(), api.roads(), api.hospitals()]);
    state.nodes = nodes; state.hospitals = hospitals;
    $('#f-node').innerHTML = nodes.map(n => `<option value="${n.id}">${esc(n.name)}</option>`).join('');
    initMap(nodes, roads, hospitals, (id) => { $('#f-node').value = id; toast('Location set'); });
  } catch (e) { toast('Cannot reach backend: ' + e.message, true); return; }

  $('#f-sev').addEventListener('input', (e) => $('#sev-val').textContent = e.target.value);
  $('#emergency-form').addEventListener('submit', (e) => {
    e.preventDefault();
    dispatch($('#f-type').value, $('#f-sev').value, $('#f-node').value);
  });
  document.querySelectorAll('[data-sc]').forEach(b => b.addEventListener('click', () => scenario(b.dataset.sc)));
  $('#btn-reset').addEventListener('click', async () => {
    try { await api.reset(); location.reload(); } catch (e) { toast('Reset failed: ' + e.message, true); }
  });
  $('#btn-csv').addEventListener('click', exportCsv);
  document.querySelectorAll('.tab').forEach(t => t.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(x => x.classList.toggle('active', x === t));
    $('#tab-log').hidden = t.dataset.tab !== 'log';
    $('#tab-reports').hidden = t.dataset.tab !== 'reports';
    if (t.dataset.tab === 'reports') renderReports();
  }));

  refresh();
  setInterval(refresh, 1500);
}
init();
