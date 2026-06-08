const API_URL = "http://localhost:8080";

window.apiGet = async function (path) {
    const response = await fetch(`${API_URL}${path}`);

    if (!response.ok) {
        throw new Error(`GET ${path} failed: ${response.status}`);
    }

    return await response.json();
};

window.apiPost = async function (path, body) {
    const response = await fetch(`${API_URL}${path}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        const message = await response.text();

        throw new Error(
            message || `POST ${path} failed: ${response.status}`
        );
    }

    return await response.json();
};

window.apiPut = async function (path, body) {
    const response = await fetch(`${API_URL}${path}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        throw new Error(`PUT ${path} failed: ${response.status}`);
    }

    return await response.json();
};

window.apiDelete = async function (path) {
    const response = await fetch(`${API_URL}${path}`, {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error(`DELETE ${path} failed: ${response.status}`);
    }
};

window.getEvents = function () {
    return apiGet("/api/events");
};

window.createEvent = function (event) {
    return apiPost("/api/events", event);
};

function getCurrentUserId() {
  return localStorage.getItem('userId');
}

function redirectToRegister() {
  localStorage.removeItem('userId');
  localStorage.removeItem('userEmail');
  window.location.href = '../pages/register.html';
}

async function getProfile() {
  const userId = getCurrentUserId();

  if (!userId) {
    redirectToRegister();
    return;
  }

  const response = await fetch(`${API_URL}/api/profile/${userId}`);

  if (!response.ok) {
    redirectToRegister();
    return;
  }

  return response.json();
}

async function saveQuestionnaire(payload) {
  const userId = getCurrentUserId();

  if (!userId) {
    redirectToRegister();
    return;
  }

  const response = await fetch(`${API_URL}/api/profile/${userId}/questionnaire`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || 'Ошибка сохранения анкеты');
  }

  return response.json();
}