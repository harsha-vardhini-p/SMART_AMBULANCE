/**
 * LifeGrid — Main Application Module
 * Orchestrates UI interactions, data polling, and component updates.
 *
 * This file will wire up all interactive behaviour in the next step.
 * For now it handles the severity slider display and tab switching.
 */

document.addEventListener('DOMContentLoaded', () => {
  console.log('[LifeGrid] app.js — DOM ready');

  // ── Severity Slider Live Update ─────────────────────────
  const slider  = document.getElementById('severity-slider');
  const display = document.getElementById('severity-display');

  if (slider && display) {
    slider.addEventListener('input', () => {
      const val = parseInt(slider.value, 10);
      display.textContent = val;
      slider.setAttribute('aria-valuenow', val);

      // Colour the number by severity level
      display.classList.remove('text-success', 'text-warning', 'text-critical');
      if (val <= 3)      display.classList.add('text-success');
      else if (val <= 6) display.classList.add('text-warning');
      else               display.classList.add('text-critical');
    });
  }


  // ── Tab Switching ───────────────────────────────────────
  const tabs   = document.querySelectorAll('.tab[role="tab"]');
  const panels = document.querySelectorAll('.tab-panel[role="tabpanel"]');

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      // Deactivate all
      tabs.forEach(t => {
        t.classList.remove('tab--active');
        t.setAttribute('aria-selected', 'false');
      });
      panels.forEach(p => p.classList.remove('tab-panel--active'));

      // Activate clicked
      tab.classList.add('tab--active');
      tab.setAttribute('aria-selected', 'true');
      const panelId = tab.getAttribute('aria-controls');
      const panel = document.getElementById(panelId);
      if (panel) panel.classList.add('tab-panel--active');
    });
  });


  // ── Initialise Map (stub) ────────────────────────────────
  if (typeof initMap === 'function') {
    initMap();
  }


  // ── Future: poll ambulances, update cards, etc. ──────────
  // setInterval(pollAmbulances, 1000);
  // setInterval(pollDispatches, 2000);

  console.log('[LifeGrid] UI initialised');
});
