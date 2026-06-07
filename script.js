document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.form');
  const email = form.querySelector('input[type="email"]');
  const password = form.querySelector('input[type="password"]');
  const submit = form.querySelector('button[type="submit"]');

  function update() {
    const emailVal = email.value.trim();
    const passVal = password.value.trim();
    const enabled = emailVal.length > 0 && passVal.length > 0;
    submit.disabled = !enabled;
  }

  email.addEventListener('input', update);
  password.addEventListener('input', update);

  // prevent actual submit for demo
  form.addEventListener('submit', (e) => {
    if (submit.disabled) e.preventDefault();
  });

  update();
});
