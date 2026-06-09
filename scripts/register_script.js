document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.form');
  if (!form) return;
  const email = form.querySelector('input[type="email"]');
  const password = form.querySelector('input[type="password"]');
  const repeatPassword = form.querySelector('input[type="password"][placeholder="Повторите пароль"]');
  const submit = form.querySelector('button[type="submit"]');

  function update() {
    const enabled = email && password && repeatPassword && email.value.trim().length > 0 && password.value.trim().length > 0 && repeatPassword.value.trim().length > 0 && password.value === repeatPassword.value;
    if (submit) submit.disabled = !enabled;
  }

  if (email) email.addEventListener('input', update);
  if (password) password.addEventListener('input', update);
  if (repeatPassword) repeatPassword.addEventListener('input', update);

  form.addEventListener('submit', (e) => {
    if (submit && submit.disabled) e.preventDefault();
  });

  update();
});
