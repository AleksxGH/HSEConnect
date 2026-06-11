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

  const loginData = {
    email: emailInput.value.trim(),
    password: passwordInput.value.trim()
  };

  try {
    const user = await apiPost("/api/auth/login", loginData);

    console.log("LOGIN RESPONSE:", user);

    localStorage.setItem('userId', String(user.userId));
    localStorage.setItem('userEmail', user.email);

    console.log("SAVED userId:", localStorage.getItem("userId"));

    window.location.href = '../pages/profile.html';
  } catch (error) {
    console.error("Ошибка логина:", error);
    errorText.textContent = error.message || 'Ошибка подключения к серверу';
  }
});