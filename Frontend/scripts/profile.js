// profile.js - универсальный модуль для страницы профиля (своего и чужого)

let relationStatus = {friend: false, following: false, blocked: false, blockedByTarget: false};

// Глобальные переменные
let currentProfile = null;
let selectedEvent = null;
let currentRenderedEvents = [];
let isOwnProfile = false;
let currentEventsMode = 'my';

// Функция для создания аббревиатуры из строки
function createAbbreviation(str) {
  if (!str)
    return '';

  const original = str.trim();
  const lowerOriginal = original.toLowerCase();

  // Если строка уже является аббревиатурой (все буквы заглавные, короткая)
  if (original === original.toUpperCase() && original.length <= 5) {
    return original;
  }

  // Разбиваем на слова, убирая лишние пробелы и знаки
  const words = original.split(/[\s\-–—]+/);
  const skipWords = ['и', 'или', 'в', 'на', 'по', 'с', 'к', 'до', 'из'];

  let abbreviation = '';

  for (const word of words) {
    const lowerWord = word.toLowerCase();

    // Пропускаем предлоги и союзы, если они не единственное слово
    if (skipWords.includes(lowerWord) && words.length > 1) {
      continue;
    }

    // Берём первую букву
    abbreviation += word.charAt(0).toUpperCase();
  }

  // Если получилось слишком коротко
  if (abbreviation.length <= 1 && words.length > 1) {
    // Берём первые буквы всех слов (включая предлоги)
    abbreviation = '';
    for (const word of words) {
      abbreviation += word.charAt(0).toUpperCase();
    }
  }

  // Если всё ещё коротко - берём первые буквы из слогов
  if (abbreviation.length <= 1) {
    const syllables = original.match(/[А-ЯЁа-яё]+/g) || [];
    if (syllables.length > 1) {
      abbreviation = syllables.map(s => s.charAt(0).toUpperCase()).join('');
    } else {
      // Берём первые 3-4 буквы
      abbreviation = original.substring(0, Math.min(4, original.length)).toUpperCase();
    }
  }

  return abbreviation;
}

// Функция для нормализации строки (первая буква заглавная)
function normalizeString(str) {
  if (!str)
    return '';
  const cleaned = str.trim().replace(/\s+/g, ' ');
  return cleaned.charAt(0).toUpperCase() + cleaned.slice(1).toLowerCase();
}

function capitalizeFirstLetter(str) {
  if (!str)
    return '';
  const cleaned = str.trim().replace(/\s+/g, ' ');
  return cleaned.charAt(0).toUpperCase() + cleaned.slice(1).toLowerCase();
}

// Функция для приведения всех первых букв слов в строке к верхнему регистру
function capitalizeWords(str) {
  if (!str)
    return '';
  return str.split(' ')
      .map(word => {
        if (word.length === 0)
          return word;
        // Сохраняем аббревиатуры (все буквы заглавные)
        if (word === word.toUpperCase() && word.length > 1) {
          return word;
        }
        return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
      })
      .join(' ');
}

// Функция для построения тегов (только факультет, программа, курс для студента / департамент, должность для сотрудника)
function buildTags(profile) {
  const tags = [];

  if (profile.student) {
    const studentParts = [];

    // Факультет (с автоматической аббревиатурой)
    if (profile.student.faculty) {
      studentParts.push(createAbbreviation(profile.student.faculty));
    }

    // Образовательная программа (с автоматической аббревиатурой)
    if (profile.student.educationProgram || profile.student.program) {
      const program = profile.student.educationProgram || profile.student.program;
      studentParts.push(createAbbreviation(program));
    }

    // Курс
    if (profile.student.course) {
      studentParts.push(profile.student.course);
    }

    if (studentParts.length > 0) {
      tags.push(studentParts.join(' · '));
    }
  } else if (profile.employee?.jobs?.length) {
    profile.employee.jobs.forEach(job => {
      const jobParts = [];

      if (job.department) {
        jobParts.push(capitalizeWords(job.department));
      }
      if (job.position) {
        jobParts.push(capitalizeWords(job.position));
      }

      if (jobParts.length > 0) {
        tags.push(jobParts.join(' · '));
      }
    });
  }

  return tags;
}

// Инициализация страницы
document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const userIdFromUrl = urlParams.get('id');
  const currentUserId = localStorage.getItem('userId');

  // Определяем, свой это профиль или чужой
  if (!userIdFromUrl || Number(userIdFromUrl) === Number(currentUserId)) {
    isOwnProfile = true;
    initProfileEditModal();
  } else {
    isOwnProfile = false;
    // Скрываем кнопку редактирования
    const editBtn = document.getElementById('openEditProfileBtn');
    if (editBtn)
      editBtn.style.display = 'none';
  }

  initEventTabs();
  initDetailsModal();
  initAvatar();

  if (!currentUserId) {
    window.location.href = 'auth.html';
    return;
  }

  try {
    let profile;

    if (isOwnProfile) {
      profile = await getProfile();
    } else {
      // Загружаем профиль другого пользователя
      const response = await fetch(`${API_URL}/api/profile/${userIdFromUrl}`);
      if (!response.ok)
        throw new Error('Профиль не найден');
      profile = await response.json();
    }

    currentProfile = profile;

    const targetUserId = getTargetUserId();

    if (targetUserId && targetUserId !== 'null' && !Number.isNaN(Number(targetUserId))) {
      await loadRelationStatus();
    }

    renderProfile(profile);

    // Загружаем события
    if (isOwnProfile) {
      await loadMyEvents();
    } else {
      await loadUserEvents(userIdFromUrl);
    }

  } catch (error) {
    console.error('Ошибка загрузки профиля:', error);

    if (error.message?.includes('404') || error.message?.includes('не найден')) {
      if (isOwnProfile) {
        localStorage.removeItem('userId');
        localStorage.removeItem('token');
        window.location.href = 'auth.html';
      } else {
        document.querySelector('.name').textContent = 'Профиль не найден';
        document.querySelector('.description').textContent = 'Пользователь не существует';
      }
    }
  }
});

// Загрузка событий пользователя (для чужого профиля)
async function loadUserEvents(profileUserId) {
  try {
    const viewerId = localStorage.getItem('userId');

    const events = await apiGet(`/api/events/user/${profileUserId}?viewerId=${viewerId}`);

    currentEventsMode = 'my';
    renderEvents(events);
    updateTabs('my');

    const goingTab = document.getElementById('goingEventsTab');
    if (goingTab)
      goingTab.style.display = 'none';

  } catch (error) {
    console.error('Ошибка загрузки событий пользователя:', error);
    renderEvents([]);
  }
}

// Инициализация аватарки
async function initAvatar() {
  const avatarContainer = document.getElementById('avatarContainer');
  if (!avatarContainer)
    return;

  if (window.avatarAPI && currentProfile) {
    const userId =
        isOwnProfile ? localStorage.getItem('userId') : new URLSearchParams(window.location.search).get('id');
    const firstName = currentProfile.firstName || '';
    const lastName = currentProfile.lastName || '';

    // Передаём параметр isOwnProfile в renderAvatar
    await window.avatarAPI.renderAvatar(avatarContainer, userId, firstName, lastName, 'medium', isOwnProfile);
  }
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

async function uploadProfileAvatar() {
  const input = document.getElementById('editAvatar');
  const userId = localStorage.getItem('userId');

  if (!input || !input.files || input.files.length === 0) {
    return null;
  }

  const formData = new FormData();
  formData.append('file', input.files[0]);

  const response = await fetch(`${API_URL}/api/users/${userId}/avatar`, {method: 'POST', body: formData});

  if (!response.ok) {
    throw new Error(await response.text());
  }

  return await response.text();
}

function renderEvents(events) {
  currentRenderedEvents = events || [];

  const container = document.getElementById('eventsContainer');
  if (!container)
    return;

  if (!events || events.length === 0) {
    container.innerHTML = `<div class="empty-events">Пока нет событий</div>`;
    return;
  }

  container.innerHTML = events
                            .map(
                                event => `
    <article class="event-card" onclick="viewEvent(${event.id})">
      <div>
        <span class="event-badge">${event.category || event.type || 'Событие'}</span>

        <h3 class="event-title">${event.title || 'Без названия'}</h3>

        <div class="event-meta">
          <span>${formatEventDate(event)}</span>
          <span>${event.location || event.address || 'Место не указано'}</span>
          <span>${event.respondedUserIds?.length || 0} участника</span>
        </div>
      </div>

      <img class="event-photo" src="../stubs/photo_square.svg" alt="Фото события">

      <div class="event-actions">
        ${
                                    currentEventsMode === 'my' ? `
              <button
    class="btn"
    onclick="event.stopPropagation(); editEvent(${event.id})">
    Редактировать
</button>

<button
    class="btn danger"
    onclick="event.stopPropagation(); deleteEvent(${event.id})">
    Удалить
</button>
            ` :
                                                                 `
              <button
    class="btn"
    onclick="event.stopPropagation(); viewEvent(${event.id})">
    Подробнее
</button>

<button
    class="btn danger"
    onclick="event.stopPropagation(); cancelGoing(${event.id})">
    Отменить
</button>
            `}
      </div>
    </article>
  `).join('');
}

async function cancelGoing(eventId) {
  if (!confirm('Отменить участие в событии?'))
    return;

  const userId = localStorage.getItem('userId');

  try {
    await apiDelete(`/api/events/${eventId}/respond?userId=${userId}`);
    await loadGoingEvents();
  } catch (error) {
    console.error('Ошибка отмены участия:', error);
    alert(error.message || 'Не удалось отменить участие');
  }
}

async function deleteEvent(eventId) {
  if (!confirm('Удалить событие?'))
    return;

  try {
    await apiDelete(`/api/events/${eventId}`);
    await loadMyEvents();
  } catch (error) {
    console.error('Ошибка удаления события:', error);
    alert(error.message || 'Не удалось удалить событие');
  }
}

function formatEventDate(event) {
  const months = [
    'января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'
  ];

  let dateObj = null;
  let timeStr = null;

  // Определяем источник данных
  if (event.date && event.time) {
    dateObj = new Date(event.date);
    timeStr = event.time;
  } else if (event.startsAt) {
    dateObj = new Date(event.startsAt);
    const hours = dateObj.getHours().toString().padStart(2, '0');
    const minutes = dateObj.getMinutes().toString().padStart(2, '0');
    timeStr = `${hours}:${minutes}`;
  } else {
    return 'Дата не указана';
  }

  // Проверка на валидность даты
  if (isNaN(dateObj.getTime())) {
    return 'Дата не указана';
  }

  const day = dateObj.getDate();
  const month = months[dateObj.getMonth()];
  const year = dateObj.getFullYear();

  return `${day} ${month} ${year}, ${timeStr}`;
}

function getDay(date) {
  if (!date)
    return '';
  return new Date(date).getDate().toString();
}

function getMonth(date) {
  if (!date)
    return '';
  return new Date(date).toLocaleString('ru', {month: 'short'}).replace('.', '');
}

function escapeHtml(str) {
  if (!str)
    return '';

  return String(str).replace(/[&<>]/g, function(m) {
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
  const detailsModal = document.getElementById('detailsModal');
  const closeDetailsBtn = document.getElementById('closeDetailsBtn');

  if (!detailsModal || !closeDetailsBtn)
    return;

  closeDetailsBtn.addEventListener('click', closeDetailsModal);

  detailsModal.addEventListener('click', (event) => {
    if (event.target === detailsModal) {
      closeDetailsModal();
    }
  });
}

function viewEvent(eventId) {
  window.location.href = `event-details.html?id=${eventId}`;
}

function closeDetailsModal() {
  document.getElementById('detailsModal').classList.remove('active');
  selectedEvent = null;
}

// Функция для обновления аватарки
// Обновление отображения аватарки
async function updateAvatarDisplay() {
  const avatarContainer = document.getElementById('avatarContainer');
  if (avatarContainer && currentProfile && window.avatarAPI) {
    const userId =
        isOwnProfile ? localStorage.getItem('userId') : new URLSearchParams(window.location.search).get('id');
    const firstName = currentProfile.firstName || '';
    const lastName = currentProfile.lastName || '';

    // Передаём параметр isOwnProfile в renderAvatar
    await window.avatarAPI.renderAvatar(avatarContainer, userId, firstName, lastName, 'medium', isOwnProfile);
  }
}

// Инициализация вкладок событий
function initEventTabs() {
  const myTab = document.getElementById('myEventsTab');
  const goingTab = document.getElementById('goingEventsTab');

  if (myTab)
    myTab.addEventListener('click', () => {
      if (isOwnProfile) {
        loadMyEvents();
      } else {
        const userId = new URLSearchParams(window.location.search).get('id');
        loadUserEvents(userId);
      }
    });

  if (goingTab && isOwnProfile) {
    goingTab.addEventListener('click', loadGoingEvents);
  }
}

// Загрузка моих событий
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

// Загрузка событий, на которые записан
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

// Обновление активной вкладки
function updateTabs(activeTab) {
  const myTab = document.getElementById('myEventsTab');
  const goingTab = document.getElementById('goingEventsTab');

  if (myTab) {
    myTab.classList.toggle('active', activeTab === 'my');
    myTab.classList.toggle('inactive', activeTab !== 'my');
  }

  if (goingTab) {
    goingTab.classList.toggle('active', activeTab === 'going');
    goingTab.classList.toggle('inactive', activeTab !== 'going');
  }
}

// Рендер профиля
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

  // Обновляем аватарку
  initRelationButtons();
  initMessageButton();
  updateAvatarDisplay();
}

// Формирование ФИО
function buildName(profile) {
  return [profile.lastName, profile.firstName, profile.middleName].filter(item => item && item.trim() !== '').join(' ');
}

// Рендер событий
function renderEvents(events) {
  currentRenderedEvents = events || [];
  const container = document.getElementById('eventsContainer');
  if (!container)
    return;

  if (!events || events.length === 0) {
    container.innerHTML = `<div class="empty-events">Пока нет событий</div>`;
    return;
  }

  container.innerHTML = events
                            .map(event => `
        <article class="event-card" onclick="viewEvent(${event.id})">
            <div>
                <span class="event-badge">${event.category || event.type || 'Событие'}</span>
                <h3 class="event-title">${escapeHtml(event.title || 'Без названия')}</h3>
                <div class="event-meta">
                    <span>${formatEventDate(event)}</span>
                    <span>${event.location || event.address || 'Место не указано'}</span>
                    <span>${event.respondedUserIds?.length || 0} участника</span>
                </div>
            </div>
            <img class="event-photo" src="../stubs/photo_square.svg" alt="Фото события">
            <div class="event-actions">
                ${getEventActions(event)}
            </div>
        </article>
    `).join('');
}

// Получение кнопок для события в зависимости от режима
function getEventActions(event) {
  if (isOwnProfile) {
    if (currentEventsMode === 'my') {
      return `
                <button class="btn" onclick="event.stopPropagation(); editEvent(${event.id})">Редактировать</button>
                <button class="btn danger" onclick="event.stopPropagation(); deleteEvent(${event.id})">Удалить</button>
            `;
    } else {
      return `
                <button class="btn" onclick="event.stopPropagation(); viewEvent(${event.id})">Подробнее</button>
                <button class="btn danger" onclick="event.stopPropagation(); cancelGoing(${event.id})">Отменить</button>
            `;
    }
  } else {
    return `
            <button class="btn" onclick="event.stopPropagation(); viewEvent(${event.id})">Подробнее</button>
        `;
  }
}

// Отмена участия в событии
async function cancelGoing(eventId) {
  if (!confirm('Отменить участие в событии?'))
    return;

  const userId = localStorage.getItem('userId');
  try {
    await apiDelete(`/api/events/${eventId}/respond?userId=${userId}`);
    await loadGoingEvents();
  } catch (error) {
    console.error('Ошибка отмены участия:', error);
    alert(error.message || 'Не удалось отменить участие');
  }
}

// Удаление события
async function deleteEvent(eventId) {
  if (!confirm('Удалить событие?'))
    return;

  try {
    await apiDelete(`/api/events/${eventId}`);
    await loadMyEvents();
  } catch (error) {
    console.error('Ошибка удаления события:', error);
    alert(error.message || 'Не удалось удалить событие');
  }
}

// Форматирование даты события
function formatEventDate(event) {
  const months = [
    'января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'
  ];

  let dateObj = null;
  let timeStr = null;

  if (event.date && event.time) {
    dateObj = new Date(event.date);
    timeStr = event.time;
  } else if (event.startsAt) {
    dateObj = new Date(event.startsAt);
    const hours = dateObj.getHours().toString().padStart(2, '0');
    const minutes = dateObj.getMinutes().toString().padStart(2, '0');
    timeStr = `${hours}:${minutes}`;
  } else {
    return 'Дата не указана';
  }

  if (isNaN(dateObj.getTime())) {
    return 'Дата не указана';
  }

  const day = dateObj.getDate();
  const month = months[dateObj.getMonth()];
  const year = dateObj.getFullYear();

  return `${day} ${month} ${year}, ${timeStr}`;
}

// Получение профиля текущего пользователя
async function getProfile() {
  const userId = localStorage.getItem('userId');
  if (!userId)
    throw new Error('Пользователь не найден');
  return await apiGet(`/api/profile/${userId}`);
}

// Обновление профиля
async function updateProfile(profile) {
  const userId = localStorage.getItem('userId');
  if (!userId)
    throw new Error('Пользователь не найден');
  return await apiPut(`/api/profile/${userId}`, profile);
}

// Инициализация модального окна редактирования профиля
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
    if (event.target === modal)
      closeProfileModal();
  });
}

// Открытие модального окна редактирования
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

// Закрытие модального окна
function closeProfileModal() {
  document.getElementById('profileModal').classList.remove('active');
}

// Сохранение изменений профиля
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

// Инициализация модального окна деталей события
function initDetailsModal() {
  const detailsModal = document.getElementById('detailsModal');
  const closeDetailsBtn = document.getElementById('closeDetailsBtn');

  if (!detailsModal || !closeDetailsBtn)
    return;

  closeDetailsBtn.addEventListener('click', closeDetailsModal);
  detailsModal.addEventListener('click', (event) => {
    if (event.target === detailsModal)
      closeDetailsModal();
  });
}

// Просмотр события
function viewEvent(eventId) {
  window.location.href = `event-details.html?id=${eventId}`;
}

// Редактирование события
function editEvent(eventId) {
  window.location.href = `event-details.html?id=${eventId}&mode=edit`;
}

// Закрытие модального окна деталей
function closeDetailsModal() {
  document.getElementById('detailsModal').classList.remove('active');
  selectedEvent = null;
}

async function loadRelationStatus() {
  const currentUserId = localStorage.getItem('userId');
  const targetUserId = getTargetUserId();

  if (!targetUserId || targetUserId === 'null' || Number.isNaN(Number(targetUserId))) {
    return;
  }

  const response = await fetch(`${API_URL}/api/friends/${currentUserId}/status/${targetUserId}`);

  if (!response.ok) {
    throw new Error(await response.text());
  }

  relationStatus = await response.json();
  console.log('RELATION STATUS', relationStatus);


  updateRelationButtons();
}

function initRelationButtons() {
  const followBtn = document.getElementById('followBtn');
  const friendBtn = document.getElementById('friendBtn');
  const blockBtn = document.getElementById('blockBtn');

  if (followBtn) {
    followBtn.onclick = handleFollowClick;
  }

  if (friendBtn) {
    friendBtn.onclick = handleFriendClick;
  }

  if (blockBtn) {
    blockBtn.onclick = handleBlockClick;
  }

  updateRelationButtons();
}

function updateRelationButtons() {
  const followBtn = document.getElementById('followBtn');
  const friendBtn = document.getElementById('friendBtn');
  const blockBtn = document.getElementById('blockBtn');

  const followText = document.getElementById('followText');
  const friendText = document.getElementById('friendText');
  const blockText = document.getElementById('blockText');

  const followIcon = document.getElementById('followIcon');
  const friendIcon = document.getElementById('friendIcon');
  const blockIcon = document.getElementById('blockIcon');

  if (relationStatus.blockedByTarget) {
    if (followText)
      followText.textContent = 'Недоступно';
    if (friendText)
      friendText.textContent = 'Недоступно';
    if (blockText)
      blockText.textContent = 'Недоступно';

    if (followBtn)
      followBtn.disabled = true;
    if (friendBtn)
      friendBtn.disabled = true;
    if (blockBtn)
      blockBtn.disabled = true;

    return;
  }

  if (relationStatus.blocked) {
    if (followText)
      followText.textContent = 'Заблокирован';
    if (friendText)
      friendText.textContent = 'Заблокирован';
    if (blockText)
      blockText.textContent = 'Разблокировать';

    if (followBtn)
      followBtn.disabled = true;
    if (friendBtn)
      friendBtn.disabled = true;
    if (blockBtn)
      blockBtn.disabled = false;

    if (blockIcon)
      blockIcon.src = '../icons/unblock_icon.svg';
    return;
  }

  if (followText) {
    followText.textContent = relationStatus.following ? 'Отписаться' : 'Подписаться';
  }

  if (friendText) {
    friendText.textContent = relationStatus.friend ? 'Удалить из друзей' : 'Добавить в друзья';
  }

  if (blockText) {
    blockText.textContent = 'Заблокировать';
  }

  if (followBtn)
    followBtn.disabled = false;
  if (friendBtn)
    friendBtn.disabled = false;
  if (blockBtn)
    blockBtn.disabled = false;

  if (followIcon) {
    followIcon.src = relationStatus.following ? '../icons/unfollow_icon.svg' : '../icons/follow_icon.svg';
  }

  if (friendIcon) {
    friendIcon.src = relationStatus.friend ? '../icons/unfriend_icon.svg' : '../icons/friend_icon.svg';
  }

  if (blockIcon) {
    blockIcon.src = '../icons/block_icon.svg';
  }
}

async function handleFollowClick() {
  const currentUserId = localStorage.getItem('userId');
  const targetUserId = getTargetUserId();

  try {
    const method = relationStatus.following ? 'DELETE' : 'POST';

    const response = await fetch(`${API_URL}/api/friends/${currentUserId}/follow/${targetUserId}`, {method: method});

    if (!response.ok) {
      throw new Error(await response.text());
    }

    await loadRelationStatus();
    await reloadCountersOnly();
  } catch (error) {
    console.error('Ошибка подписки:', error);
    alert(error.message || 'Не удалось выполнить действие');
  }
}

async function handleFriendClick() {
  const currentUserId = localStorage.getItem('userId');
  const targetUserId = getTargetUserId();

  try {
    const url = relationStatus.friend ? `${API_URL}/api/friends/${currentUserId}/friend/${targetUserId}` :
                                        `${API_URL}/api/friends/${currentUserId}/add/${targetUserId}`;

    const method = relationStatus.friend ? 'DELETE' : 'POST';

    const response = await fetch(url, {method});

    if (!response.ok) {
      throw new Error(await response.text());
    }

    await loadRelationStatus();
    await reloadCountersOnly();
  } catch (error) {
    console.error('Ошибка действия с друзьями:', error);
    alert(error.message || 'Не удалось выполнить действие');
  }
}

async function handleBlockClick() {
  const currentUserId = localStorage.getItem('userId');
  const targetUserId = getTargetUserId();

  console.log('BLOCK CLICK');
  console.log('currentUserId =', currentUserId);
  console.log('targetUserId =', targetUserId);
  console.log('relationStatus =', relationStatus);

  if (!currentUserId || !targetUserId) {
    alert('Не удалось определить пользователя для блокировки');
    return;
  }

  try {
    const method = relationStatus.blocked ? 'DELETE' : 'POST';

    const url = `${API_URL}/api/blocks/${currentUserId}/${targetUserId}`;

    console.log('BLOCK REQUEST:', method, url);

    const response = await fetch(url, {method: method});

    if (!response.ok) {
      throw new Error(await response.text());
    }

    await loadRelationStatus();
    updateRelationButtons();
    initMessageButton();

  } catch (error) {
    console.error('Ошибка блокировки:', error);
    alert(error.message || 'Не удалось выполнить блокировку');
  }
}

function getTargetUserId() {
  const params = new URLSearchParams(window.location.search);

  return (params.get('id') || params.get('userId') || params.get('profileUserId'));
}

function hideActionsBlock() {
  const actionsBlock = document.getElementById('actionsBlock');
  if (actionsBlock) {
    actionsBlock.style.display = 'none';
  }
}

function setTextSafe(id, text) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = text;
  }
}

async function reloadCountersOnly() {
  const targetUserId = getTargetUserId();

  try {
    const response = await fetch(`${API_URL}/api/profile/${targetUserId}`);

    if (!response.ok)
      return;

    const profile = await response.json();

    setTextSafe('friendsCount', profile.friendsCount ?? 0);
    setTextSafe('followersCount', profile.followersCount ?? 0);
  } catch (error) {
    console.error('Не удалось обновить счётчики:', error);
  }
}

function initMessageButton() {
  const messageBtn = document.getElementById('messageBtn');
  if (!messageBtn)
    return;

  if (relationStatus.blocked || relationStatus.blockedByTarget) {
    messageBtn.disabled = true;
    messageBtn.style.opacity = '0.5';
    messageBtn.style.cursor = 'not-allowed';

    const span = messageBtn.querySelector('span');
    if (span) {
      span.textContent = 'Недоступно';
    }

    messageBtn.onclick = () => {
      alert('Нельзя написать пользователю, который вас заблокировал');
    };

    return;
  }

  messageBtn.disabled = false;
  messageBtn.style.opacity = '1';
  messageBtn.style.cursor = 'pointer';

  const span = messageBtn.querySelector('span');
  if (span) {
    span.textContent = 'Написать';
  }

  messageBtn.onclick = () => {
    const targetUserId = getTargetUserId();

    if (!targetUserId) {
      alert('Не удалось определить пользователя');
      return;
    }

    window.location.href = `chat.html?userId=${targetUserId}`;
  };
}

// Escape HTML
function escapeHtml(str) {
  if (!str)
    return '';
  return String(str).replace(/[&<>]/g, function(m) {
    if (m === '&')
      return '&amp;';
    if (m === '<')
      return '&lt;';
    if (m === '>')
      return '&gt;';
    return m;
  });
}
