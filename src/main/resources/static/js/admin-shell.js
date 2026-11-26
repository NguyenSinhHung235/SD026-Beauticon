'use strict';
document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.getElementById('admin-menu-toggle');
  const sidebar = document.getElementById('admin-sidebar-links');
  if (!toggle || !sidebar) return;
  const setOpen = open => {
    sidebar.classList.toggle('is-open', open);
    toggle.setAttribute('aria-expanded', String(open));
  };
  toggle.addEventListener('click', () => setOpen(!sidebar.classList.contains('is-open')));
  sidebar.querySelectorAll('a').forEach(a => a.addEventListener('click', () => setOpen(false)));
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && sidebar.classList.contains('is-open')) {
      setOpen(false);
      toggle.focus();
    }
  });
  const media = window.matchMedia('(min-width: 841px)');
  media.addEventListener?.('change', () => setOpen(false));
});
