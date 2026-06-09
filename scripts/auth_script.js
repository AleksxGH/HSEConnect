document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.form');
  if (!form) return;
  const email = form.querySelector('input[type="email"]');
  const password = form.querySelector('input[type="password"]');
  const submit = form.querySelector('button[type="submit"]');

  function update() {
    const enabled = email && password && email.value.trim().length > 0 && password.value.trim().length > 0;
    if (submit) submit.disabled = !enabled;
  }

  if (email) email.addEventListener('input', update);
  if (password) password.addEventListener('input', update);

  form.addEventListener('submit', (e) => {
    if (submit && submit.disabled) e.preventDefault();
  });

  update();
});
