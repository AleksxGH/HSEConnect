const API_URL = 'http://localhost:8080';

const form = document.getElementById('loginForm');
const emailInput = document.getElementById('emailInput');
const passwordInput = document.getElementById('passwordInput');
const loginButton = document.getElementById('loginButton');
const errorText = document.getElementById('errorText');

function checkForm() {
  loginButton.disabled = emailInput.value.trim() === '' || passwordInput.value.trim() === '';
}

emailInput.addEventListener('input', checkForm);
passwordInput.addEventListener('input', checkForm);

form.addEventListener('submit', async function(event) {
  event.preventDefault();

  errorText.textContent = '';

  const loginData = {email: emailInput.value.trim(), password: passwordInput.value.trim()};

  try {
    const response = await fetch(
        `${API_URL}/api/auth/login`,
        {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(loginData)});

    if (!response.ok) {
      const message = await response.text();
      console.log('Ошибка от backend:', message);
      errorText.textContent = message;
      return;
    }

    const user = await response.json();

    localStorage.setItem('userId', user.userId);
    localStorage.setItem('userEmail', user.email);

    window.location.href = '../pages/profile.html';

  } catch (error) {
    errorText.textContent = 'Ошибка подключения к серверу';
  }
  console.log("auth_script.js подключился");
});