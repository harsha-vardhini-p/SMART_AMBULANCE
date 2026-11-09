/**
 * LifeGrid — API Client Module
 * Handles all REST API communication with the Spring Boot backend.
 *
 * This file will be populated with fetch wrappers in the next step.
 * For now it exports endpoint constants and function stubs.
 */

// ── Base URL ────────────────────────────────────────────────
const API_BASE = '';  // same origin, no prefix needed

// ── Endpoint Constants ──────────────────────────────────────
const API = Object.freeze({
  EMERGENCIES:      '/api/emergencies',
  AMBULANCES:       '/api/ambulances',
  HOSPITALS:        '/api/hospitals',
  NODES:            '/api/nodes',
  ROADS:            '/api/roads',
  DISPATCHES:       '/api/dispatches',
  REPORT_SUMMARY:   '/api/reports/summary',
  SIMULATION_RESET: '/api/simulation/reset',
});

// ── Generic Fetch Wrapper (stub) ────────────────────────────
// async function apiFetch(endpoint, options = {}) { ... }

// ── Endpoint Functions (stubs) ──────────────────────────────
// async function createEmergency(data) { ... }
// async function fetchEmergencies() { ... }
// async function fetchAmbulances() { ... }
// async function fetchHospitals() { ... }
// async function fetchNodes() { ... }
// async function fetchRoads() { ... }
// async function fetchDispatches() { ... }
// async function fetchSummaryReport() { ... }
// async function resetSimulation() { ... }

console.log('[LifeGrid] api.js loaded — endpoints ready');
