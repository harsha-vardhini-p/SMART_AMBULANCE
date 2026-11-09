const COLORS = { primary: '#3B82F6', critical: '#EF4444', warning: '#F59E0B', success: '#22C55E', accent: '#14B8A6' };
let map, nodeLayer, roadLayer, hosLayer, ambLayer, emgLayer, routeLayer;
const ambMarkers = new Map();
let onNodeClick = () => {};

const dot = (color, size = 14, cls = '') => L.divIcon({
  className: '',
  html: `<div class="${cls}" style="width:${size}px;height:${size}px;border-radius:50%;background:${color};border:2px solid #0F172A;box-shadow:0 0 0 2px ${color}55"></div>`,
  iconSize: [size, size], iconAnchor: [size / 2, size / 2],
});
const square = (color) => L.divIcon({
  className: '',
  html: `<div style="width:20px;height:20px;border-radius:6px;background:${color};color:#0F172A;font-weight:700;display:grid;place-items:center;border:2px solid #0F172A">H</div>`,
  iconSize: [20, 20], iconAnchor: [10, 10],
});

export function initMap(nodes, roads, hospitals, nodeClickCb) {
  onNodeClick = nodeClickCb;
  map = L.map('map', { zoomControl: true });
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors', maxZoom: 19,
  }).addTo(map);
  roadLayer = L.layerGroup().addTo(map);
  nodeLayer = L.layerGroup().addTo(map);
  hosLayer = L.layerGroup().addTo(map);
  routeLayer = L.layerGroup().addTo(map);
  emgLayer = L.layerGroup().addTo(map);
  ambLayer = L.layerGroup().addTo(map);

  const byId = new Map(nodes.map(n => [n.id, n]));
  roads.forEach(r => {
    const a = byId.get(r.fromNodeId), b = byId.get(r.toNodeId);
    if (a && b) L.polyline([[a.latitude, a.longitude], [b.latitude, b.longitude]],
      { color: '#475569', weight: 3, opacity: .8 }).bindTooltip(`${r.distanceKm} km`).addTo(roadLayer);
  });
  nodes.forEach(n => {
    L.circleMarker([n.latitude, n.longitude], { radius: 5, color: '#64748B', fillColor: '#0F172A', fillOpacity: 1, weight: 2 })
      .bindTooltip(n.name).on('click', () => onNodeClick(n.id)).addTo(nodeLayer);
  });
  hospitals.forEach(h => {
    const n = byId.get(h.nodeId);
    if (n) L.marker([n.latitude, n.longitude], { icon: square(COLORS.accent) }).bindTooltip(h.name).addTo(hosLayer);
  });
  const pts = nodes.map(n => [n.latitude, n.longitude]);
  if (pts.length) map.fitBounds(pts, { padding: [30, 30] });
  setTimeout(() => map.invalidateSize(), 200);
}

export function updateAmbulances(list) {
  const seen = new Set();
  list.forEach(a => {
    seen.add(a.id);
    const color = a.status === 'AVAILABLE' ? COLORS.primary : COLORS.warning;
    const pos = [a.latitude, a.longitude];
    let m = ambMarkers.get(a.id);
    if (!m) {
      m = L.marker(pos, { icon: dot(color, 16), zIndexOffset: 1000 }).addTo(ambLayer);
      ambMarkers.set(a.id, m);
    } else {
      m.setLatLng(pos); m.setIcon(dot(color, 16));
    }
    m.bindTooltip(`${a.callSign} · ${a.type} · ${a.status}`);
  });
  [...ambMarkers.keys()].forEach(id => { if (!seen.has(id)) { ambLayer.removeLayer(ambMarkers.get(id)); ambMarkers.delete(id); } });
}

export function updateEmergencies(emergencies, nodes, ambulances, hospitals) {
  emgLayer.clearLayers(); routeLayer.clearLayers();
  const nById = new Map(nodes.map(n => [n.id, n]));
  const aById = new Map(ambulances.map(a => [a.id, a]));
  const hById = new Map(hospitals.map(h => [h.id, h]));
  emergencies.filter(e => e.status !== 'COMPLETED').forEach(e => {
    const scene = nById.get(e.locationNodeId);
    if (!scene) return;
    const sp = [scene.latitude, scene.longitude];
    L.marker(sp, { icon: dot(COLORS.critical, 18, 'pulse') }).bindTooltip(`${e.type} · severity ${e.severity}`).addTo(emgLayer);
    const amb = aById.get(e.ambulanceId), hos = hById.get(e.hospitalId);
    const hn = hos && nById.get(hos.nodeId);
    const pts = [];
    if (amb) pts.push([amb.latitude, amb.longitude]);
    pts.push(sp);
    if (hn) pts.push([hn.latitude, hn.longitude]);
    if (pts.length > 1) L.polyline(pts, { color: COLORS.primary, weight: 3, dashArray: '6 8' }).addTo(routeLayer);
  });
}
