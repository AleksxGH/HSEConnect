document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.form');
  if (!form) return;

  const email = form.querySelector('input[type="email"]');
  const passwords = form.querySelectorAll('input[type="password"]');

  const password = passwords[0];
  const repeatPassword = passwords[1];

  const submit = form.querySelector('button[type="submit"]');
  const error = form.querySelector('.password-mismatch');

  function update() {
    const samePasswords = password.value === repeatPassword.value;

    const enabled =
      email.value.trim() !== '' &&
      password.value.trim() !== '' &&
      repeatPassword.value.trim() !== '' &&
      samePasswords;

    submit.disabled = !enabled;

    if (error) {
      error.style.display =
        repeatPassword.value.trim() !== '' && !samePasswords
          ? 'block'
          : 'none';
    }
  }

  email.addEventListener('input', update);
  password.addEventListener('input', update);
  repeatPassword.addEventListener('input', update);

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (submit.disabled) return;

    try {
      const user = await apiPost('/api/auth/register', {
        email: email.value.trim(),
        password: password.value.trim()
      });

      localStorage.setItem('userId', user.userId);
      localStorage.setItem('userEmail', user.email);

      window.location.href = '../pages/questionnaire.html';
    } catch (err) {
      alert(err.message || 'Ошибка регистрации');
    }
  });

  update();
});