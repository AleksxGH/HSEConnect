document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.form');
  if (!form)
    return;

  const email = form.querySelector('input[type="email"]');
  const passwords = form.querySelectorAll('input[type="password"]');

  const password = passwords[0];
  const repeatPassword = passwords[1];

  const submit = form.querySelector('button[type="submit"]');

  function update() {
    const enabled = email && password && repeatPassword && email.value.trim() !== '' && password.value.trim() !== '' &&
        repeatPassword.value.trim() !== '' && password.value === repeatPassword.value;

    if (submit)
      submit.disabled = !enabled;
  }

  email?.addEventListener('input', update);
  password?.addEventListener('input', update);
  repeatPassword?.addEventListener('input', update);

  form.addEventListener('submit', (e) => {
    e.preventDefault();

    if (submit.disabled)
      return;

    const user = await apiPost('/api/auth/register', {email: email.value.trim(), password: password.value.trim()});

    localStorage.setItem('userId', user.userId);
    localStorage.setItem('userEmail', user.email);

    window.location.href = '../pages/questionnaire.html';
  });

  update();
});
