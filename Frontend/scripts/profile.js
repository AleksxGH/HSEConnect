let currentProfile = null;

document.addEventListener('DOMContentLoaded', async () => {
  initProfileEditModal();

  try {
    const profile = await getProfile();

    currentProfile = profile;
    renderProfile(profile);

  } catch (error) {
    console.error('Ошибка загрузки профиля:', error);
    window.location.href = 'auth.html';
  }
});

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

  if (profile.student) {
    if (profile.student.faculty)
      tags.push(profile.student.faculty);
    if (profile.student.program)
      tags.push(profile.student.program);
    if (profile.student.course)
      tags.push(profile.student.course);
    if (profile.student.status)
      tags.push(profile.student.status);
  }

  if (profile.employee) {
    if (profile.employee.jobs && profile.employee.jobs.length > 0) {
      profile.employee.jobs.forEach(job => {
        if (job.department)
          tags.push(job.department);
        if (job.position)
          tags.push(job.position);
      });
    }
  }

  if (profile.tags && profile.tags.length > 0) {
    tags.push(...profile.tags);
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

  if (typeof apiPut === 'function') {
    return await apiPut(`/api/profile/${userId}`, profile);
  }

  const response = await fetch(
      `http://localhost:8080/api/profile/${userId}`,
      {method: 'PUT', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(profile)});

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Ошибка сохранения профиля');
  }

  return await response.json();
}