let currentProfile = null;
let currentEventsMode = 'my';
let selectedEvent = null;
let currentRenderedEvents = [];

document.addEventListener('DOMContentLoaded', async () => {
  initProfileEditModal();
  initEventTabs();
  initDetailsModal();

  const userId = localStorage.getItem('userId');

  if (!userId) {
    window.location.href = 'auth.html';
    return;
  }

  try {
    const profile = await getProfile();

    currentProfile = profile;
    renderProfile(profile);
  } catch (error) {
    console.error('Ошибка загрузки профиля:', error);
    alert(error.message || 'Профиль не найден');
    return;
  }

  try {
    await loadMyEvents();
  } catch (error) {
    console.error('Ошибка загрузки событий профиля:', error);
  }
});

function initEventTabs() {
  const myTab = document.getElementById('myEventsTab');
  const goingTab = document.getElementById('goingEventsTab');

  myTab.addEventListener('click', loadMyEvents);
  goingTab.addEventListener('click', loadGoingEvents);
}

async function loadMyEvents() {
  const userId = localStorage.getItem('userId');

  if (!userId) {
    window.location.href = 'auth.html';
    return;
  }

  currentEventsMode = 'my';

  const events = await apiGet(`/api/events/my?userId=${userId}`);
  renderEvents(events);

  updateTabs('my');
}

function updateTabs(activeTab) {
  const myTab = document.getElementById('myEventsTab');
  const goingTab = document.getElementById('goingEventsTab');

  myTab.classList.toggle('active', activeTab === 'my');
  myTab.classList.toggle('inactive', activeTab !== 'my');

  goingTab.classList.toggle('active', activeTab === 'going');
  goingTab.classList.toggle('inactive', activeTab !== 'going');
}

async function loadGoingEvents() {
  const userId = localStorage.getItem('userId');

  if (!userId) {
    window.location.href = 'auth.html';
    return;
  }

  currentEventsMode = 'going';

  const events = await apiGet(`/api/events/going?userId=${userId}`);
  renderEvents(events);

  updateTabs('going');
}

function renderProfile(profile) {
  const nameEl = document.querySelector('.name');
  const descriptionEl = document.querySelector('.description');
  const tagsEl = document.querySelector('.tags');
  const chipListEl = document.querySelector('.chip-list');

  if (nameEl) {
    nameEl.textContent = buildName(profile) || profile.name || 'Профиль';
  }

  if (descriptionEl) {
    descriptionEl.textContent = profile.about || profile.description || 'Пока нет описания';
  }

  if (tagsEl) {
    tagsEl.innerHTML = '';

    const tags = buildTags(profile);

    tags.forEach(tag => {
      const span = document.createElement('span');
      span.className = 'tag';
      span.textContent = tag;
      tagsEl.appendChild(span);
    });
  }

  if (chipListEl) {
    chipListEl.innerHTML = '';

    const interests = profile.interests || [];

    interests.forEach(interest => {
      const chip = document.createElement('span');
      chip.className = 'chip';
      chip.textContent = interest;
      chipListEl.appendChild(chip);
    });
  }

  const friendsCount = document.getElementById('friendsCount');
  const followersCount = document.getElementById('followersCount');
  const eventsCount = document.getElementById('eventsCount');

  if (friendsCount)
    friendsCount.textContent = profile.friendsCount || 0;
  if (followersCount)
    followersCount.textContent = profile.followersCount || 0;
  if (eventsCount)
    eventsCount.textContent = profile.eventsCount || 0;
}

function buildName(profile) {
  return [profile.lastName, profile.firstName, profile.middleName].filter(item => item && item.trim() !== '').join(' ');
}

function buildTags(profile) {
  const tags = [];
  const usedValues = new Set();

  // Функция для добавления значения в Set
  const markAsUsed = (value) => {
    if (value) usedValues.add(value);
  };

  if (profile.type) {
    markAsUsed(profile.type);
  }
  // Группируем данные студента
  if (profile.student) {
    const studentParts = [];

    if (profile.student.faculty) {
      studentParts.push(profile.student.faculty);
      markAsUsed(profile.student.faculty);
    }
    if (profile.student.program) {
      studentParts.push(profile.student.program);
      markAsUsed(profile.student.program);
    }
    if (profile.student.course) {
      studentParts.push(profile.student.course);
      markAsUsed(profile.student.course);
    }
    if (profile.student.status) {
      if (profile.student.status.toLowerCase() !== 'учусь') {
        studentParts.push(profile.student.status);
      }
      markAsUsed(profile.student.status);
    }
    if (profile.student.educationLevel) {
      markAsUsed(profile.student.educationLevel);
    }

    if (studentParts.length > 0) {
      tags.push(studentParts.join(' · '));
    }
  }

  // Группируем данные сотрудника
  if (profile.employee?.jobs?.length) {
    profile.employee.jobs.forEach(job => {
      const jobParts = [];

      if (job.department) {
        jobParts.push(job.department);
        markAsUsed(job.department);
      }
      if (job.position) {
        jobParts.push(job.position);
        markAsUsed(job.position);
      }

      if (jobParts.length > 0) {
        tags.push(jobParts.join(' · '));
      }
    });
  }

  // Добавляем только уникальные и неиспользованные теги
  if (profile.tags?.length) {
    const uniqueTags = [...new Set(profile.tags)]; // Убираем дубликаты
    uniqueTags.forEach(tag => {
      if (!usedValues.has(tag)) {
        tags.push(tag);
      }
    });
  }

  return tags.filter(Boolean);
}

function initProfileEditModal() {
  const openBtn = document.getElementById('openEditProfileBtn');
  const modal = document.getElementById('profileModal');
  const closeBtn = document.getElementById('closeProfileModalBtn');
  const saveBtn = document.getElementById('saveProfileBtn');

  if (!openBtn || !modal || !closeBtn || !saveBtn)
    return;

  openBtn.addEventListener('click', openProfileModal);
  closeBtn.addEventListener('click', closeProfileModal);
  saveBtn.addEventListener('click', saveProfileChanges);

  modal.addEventListener('click', (event) => {
    if (event.target === modal) {
      closeProfileModal();
    }
  });
}

function openProfileModal() {
  if (!currentProfile)
    return;

  document.getElementById('editLastName').value = currentProfile.lastName || '';
  document.getElementById('editFirstName').value = currentProfile.firstName || '';
  document.getElementById('editMiddleName').value = currentProfile.middleName || '';
  document.getElementById('editAbout').value = currentProfile.about || '';

  document.getElementById('editInterests').value =
    Array.isArray(currentProfile.interests) ? currentProfile.interests.join(', ') : '';

  document.getElementById('profileModal').classList.add('active');
}

function closeProfileModal() {
  document.getElementById('profileModal').classList.remove('active');
}

async function saveProfileChanges() {
  const updatedProfile = {
    ...currentProfile,
    lastName: document.getElementById('editLastName').value.trim(),
    firstName: document.getElementById('editFirstName').value.trim(),
    middleName: document.getElementById('editMiddleName').value.trim(),
    about: document.getElementById('editAbout').value.trim(),
    interests: document.getElementById('editInterests')
      .value.split(',')
      .map(item => item.trim())
      .filter(item => item.length > 0)
  };

  try {
    await updateProfile(updatedProfile);

    currentProfile = updatedProfile;
    renderProfile(currentProfile);

    closeProfileModal();
    alert('Профиль сохранён');
  } catch (error) {
    console.error('Ошибка сохранения профиля:', error);
    alert(error.message || 'Не удалось сохранить профиль');
  }
}

async function updateProfile(profile) {
  const userId = localStorage.getItem('userId');

  if (!userId) {
    throw new Error('Пользователь не найден');
  }

  return await apiPut(`/api/profile/${userId}`, profile);
}

function renderEvents(events) {
  currentRenderedEvents = events || [];

  const container = document.getElementById("eventsContainer");
  if (!container) return;

  if (!events || events.length === 0) {
    container.innerHTML = `<div class="empty-events">Пока нет событий</div>`;
    return;
  }

  container.innerHTML = events.map(event => `
    <article class="event-card">
      <div>
        <span class="event-badge">${event.category || event.type || "Событие"}</span>

        <h3 class="event-title">${event.title || "Без названия"}</h3>

        <div class="event-meta">
          <span>${formatEventDate(event)}</span>
          <span>${event.location || event.address || "Место не указано"}</span>
          <span>${event.respondedUserIds?.length || 0} участника</span>
        </div>
      </div>

      <img class="event-photo" src="../stubs/photo_square.svg" alt="Фото события">

      <div class="event-actions">
        ${currentEventsMode === 'my'
      ? `
              <button class="btn" onclick="editEvent(${event.id})">Редактировать</button>
              <button class="btn danger" onclick="deleteEvent(${event.id})">Удалить</button>
            `
      : `
              <button class="btn" onclick="viewEvent(${event.id})">Подробнее</button>
              <button class="btn danger" onclick="cancelGoing(${event.id})">Отменить</button>
            `
    }
      </div>
    </article>
  `).join("");
}

function viewEvent(eventId) {
  alert(`Подробнее о событии #${eventId}`);
}

async function cancelGoing(eventId) {
  if (!confirm("Отменить участие в событии?")) return;

  const userId = localStorage.getItem("userId");

  try {
    await apiDelete(`/api/events/${eventId}/respond?userId=${userId}`);
    await loadGoingEvents();
  } catch (error) {
    console.error("Ошибка отмены участия:", error);
    alert(error.message || "Не удалось отменить участие");
  }
}

async function deleteEvent(eventId) {
  if (!confirm("Удалить событие?")) return;

  try {
    await apiDelete(`/api/events/${eventId}`);
    await loadMyEvents();
  } catch (error) {
    console.error("Ошибка удаления события:", error);
    alert(error.message || "Не удалось удалить событие");
  }
}

function formatEventDate(event) {
  if (event.date && event.time) {
    return `${event.date}, ${event.time}`;
  }

  if (event.startsAt) {
    const date = new Date(event.startsAt);
    return date.toLocaleString('ru-RU', { day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit' });
  }

  return 'Дата не указана';
}

function getDay(date) {
  if (!date)
    return '';
  return new Date(date).getDate().toString();
}

function getMonth(date) {
  if (!date)
    return '';
  return new Date(date).toLocaleString('ru', { month: 'short' }).replace('.', '');
}

async function getProfile() {
  const userId = localStorage.getItem('userId');

  if (!userId) {
    throw new Error('Пользователь не найден');
  }

  return await apiGet(`/api/profile/${userId}`);
}

function escapeHtml(str) {
  if (!str)
    return '';

  return String(str).replace(/[&<>]/g, function (m) {
    if (m === '&')
      return '&amp;';
    if (m === '<')
      return '&lt;';
    if (m === '>')
      return '&gt;';
    return m;
  });
}

function initDetailsModal() {
  const detailsModal = document.getElementById("detailsModal");
  const closeDetailsBtn = document.getElementById("closeDetailsBtn");

  if (!detailsModal || !closeDetailsBtn) return;

  closeDetailsBtn.addEventListener("click", closeDetailsModal);

  detailsModal.addEventListener("click", (event) => {
    if (event.target === detailsModal) {
      closeDetailsModal();
    }
  });
}

function viewEvent(eventId) {
  const event = currentRenderedEvents.find(item => item.id === eventId);

  if (!event) {
    alert("Событие не найдено");
    return;
  }

  selectedEvent = event;

  document.getElementById("detailsTitle").textContent = event.title || "Событие";
  document.getElementById("detailsType").textContent = event.type || event.category || "";
  document.getElementById("detailsLocation").textContent = event.location || event.address || "";
  document.getElementById("detailsDate").textContent = event.date || "";
  document.getElementById("detailsTime").textContent = event.time || "";
  document.getElementById("detailsDescription").textContent =
    event.description || "Описание отсутствует";

  document.getElementById("detailsModal").classList.add("active");
}

function closeDetailsModal() {
  document.getElementById("detailsModal").classList.remove("active");
  selectedEvent = null;
}