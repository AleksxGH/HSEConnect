// scripts/event-details.js - универсальный модуль для отображения деталей события

// Глобальное состояние
let currentEvent = null;
let isUserResponded = false;
let friendsList = [];

// ========== ОСНОВНЫЕ ФУНКЦИИ ==========

// Открытие деталей события
window.openEventDetails = async function(eventId) {
    try {
        const event = await apiGet(`/api/events/${eventId}`);
        if (!event) {
            console.error('Событие не найдено');
            return;
        }
        
        currentEvent = event;
        const currentUserId = Number(localStorage.getItem('userId'));
        const isCreator = event.creatorId === currentUserId;
        
        await loadEventParticipants(eventId);
        renderEventDetailsModal(event, isCreator);
        
        const modal = document.getElementById('eventDetailsModal');
        if (modal) modal.classList.add('active');
    } catch (error) {
        console.error('Ошибка загрузки события:', error);
        alert('Не удалось загрузить информацию о событии');
    }
};

// Загрузка участников события
async function loadEventParticipants(eventId) {
    try {
        const response = await fetch(`${API_URL}/api/events/${eventId}/participants`);
        if (response.ok) {
            const participants = await response.json();
            currentEvent.participants = participants;
            currentEvent.participantsCount = participants.length;
        }
    } catch (error) {
        console.error('Ошибка загрузки участников:', error);
        currentEvent.participants = [];
        currentEvent.participantsCount = 0;
    }
}

// ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

function formatEventDateTime(date, time) {
    if (!date) return "Дата не указана";
    const dateObj = new Date(date);
    const months = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 
                    'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'];
    const formattedDate = `${dateObj.getDate()} ${months[dateObj.getMonth()]} ${dateObj.getFullYear()}`;
    return time ? `${formattedDate}, ${time}` : formattedDate;
}

function getParticipantsText(count) {
    if (count === 0) return "Нет участников";
    if (count === 1) return "1 участник";
    if (count >= 2 && count <= 4) return `${count} участника`;
    return `${count} участников`;
}

function getAvatarInitials(name) {
    if (!name) return '?';
    const parts = name.split(' ');
    if (parts.length >= 2) {
        return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return name.charAt(0).toUpperCase();
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// ========== СОЗДАНИЕ МОДАЛЬНОГО ОКНА ==========

function createModalTemplate() {
    const modal = document.createElement('div');
    modal.id = 'eventDetailsModal';
    modal.className = 'modal';
    
    modal.innerHTML = `
        <div class="modal-content event-details-modal">
            <button class="modal-close-btn" id="closeEventDetailsBtn">✕</button>
            
            <div class="event-actions-top" id="eventOwnerActions" style="display: none;">
                <button class="icon-btn edit-event-btn" id="editEventBtn">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M17 3l4 4L7 21H3v-4L17 3z"/>
                        <path d="M15 5l4 4"/>
                    </svg>
                </button>
                <button class="icon-btn delete-event-btn" id="deleteEventBtn">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                </button>
            </div>

            <div class="event-details-layout">
                <div class="event-details-info">
                    <h2 id="detailsTitle" class="event-details-title">Название события</h2>
                    
                    <div class="event-status-container">
                        <span class="event-status" id="eventStatus">Запланировано</span>
                    </div>

                    <div class="details-row">
                        <div class="details-icon">📅</div>
                        <div class="details-content">
                            <div class="details-label">Дата и время</div>
                            <div class="details-value" id="detailsDateTime"></div>
                        </div>
                    </div>

                    <div class="details-row">
                        <div class="details-icon">📍</div>
                        <div class="details-content">
                            <div class="details-label">Место проведения</div>
                            <div class="details-value" id="detailsLocation"></div>
                        </div>
                    </div>

                    <div class="details-row">
                        <div class="details-icon">👥</div>
                        <div class="details-content">
                            <div class="details-label">Участники</div>
                            <div class="details-value" id="detailsParticipants"></div>
                        </div>
                    </div>

                    <div class="details-row">
                        <div class="details-icon">🏷️</div>
                        <div class="details-content">
                            <div class="details-label">Тип события</div>
                            <div class="details-value" id="detailsType"></div>
                        </div>
                    </div>

                    <div class="details-row description-row">
                        <div class="details-icon">📝</div>
                        <div class="details-content">
                            <div class="details-label">Описание</div>
                            <div class="details-value" id="detailsDescription"></div>
                        </div>
                    </div>
                </div>

                <div class="event-details-photo">
                    <img id="eventPhoto" src="../stubs/event_placeholder.jpg" alt="Фото события">
                </div>
            </div>

            <div class="event-details-actions">
                <button class="action-btn respond-btn" id="respondEventBtn">
                    <span>✋</span> Записаться
                </button>
                <button class="action-btn invite-btn" id="inviteFriendBtn">
                    <span>👥</span> Пригласить друга
                </button>
            </div>

            <div class="invite-friends-dropdown" id="inviteFriendsDropdown" style="display: none;">
                <div class="dropdown-header">
                    <input type="text" id="friendSearchInput" placeholder="Поиск друзей..." class="friend-search-input">
                </div>
                <div class="friends-list-dropdown" id="friendsListDropdown"></div>
            </div>
        </div>
    `;
    
    return modal;
}

function renderEventDetailsModal(event, isCreator) {
    let modal = document.getElementById('eventDetailsModal');
    
    if (!modal) {
        modal = createModalTemplate();
        document.body.appendChild(modal);
    }
    
    const currentUserId = Number(localStorage.getItem('userId'));
    const isPast = new Date(event.date) < new Date();
    isUserResponded = event.participants?.includes(currentUserId) || false;
    
    // Заполнение данных
    modal.querySelector('#detailsTitle').textContent = event.title || "Событие";
    modal.querySelector('#detailsType').textContent = event.type || "Не указан";
    modal.querySelector('#detailsLocation').textContent = event.location || "Не указано";
    modal.querySelector('#detailsDescription').textContent = event.description || "Нет описания";
    modal.querySelector('#detailsDateTime').textContent = formatEventDateTime(event.date, event.time);
    modal.querySelector('#detailsParticipants').textContent = getParticipantsText(event.participantsCount || 0);
    
    const statusElement = modal.querySelector('#eventStatus');
    if (isPast) {
        statusElement.textContent = "Завершено";
        statusElement.className = "event-status past";
    } else {
        statusElement.textContent = "Запланировано";
        statusElement.className = "event-status upcoming";
    }
    
    const ownerActions = modal.querySelector('#eventOwnerActions');
    ownerActions.style.display = isCreator ? "flex" : "none";
    
    const eventPhoto = modal.querySelector('#eventPhoto');
    eventPhoto.src = event.photoUrl || '../stubs/event_placeholder.jpg';
    
    const respondBtn = modal.querySelector('#respondEventBtn');
    if (isPast) {
        respondBtn.style.display = "none";
    } else {
        respondBtn.style.display = "flex";
        if (isUserResponded) {
            respondBtn.innerHTML = '<span>❌</span> Отменить запись';
            respondBtn.classList.add("cancel");
        } else {
            respondBtn.innerHTML = '<span>✋</span> Записаться';
            respondBtn.classList.remove("cancel");
        }
    }
    
    const inviteBtn = modal.querySelector('#inviteFriendBtn');
    inviteBtn.style.display = isPast ? "none" : "flex";
    
    setupEventHandlers(modal, event, isCreator, isPast);
}

// ========== ОБРАБОТЧИКИ СОБЫТИЙ ==========

function setupEventHandlers(modal, event, isCreator, isPast) {
    // Закрытие
    modal.querySelector('#closeEventDetailsBtn').onclick = closeEventDetailsModal;
    modal.onclick = (e) => { if (e.target === modal) closeEventDetailsModal(); };
    
    // Кнопка записи/отмены
    const respondBtn = modal.querySelector('#respondEventBtn');
    respondBtn.onclick = () => {
        if (isPast) return;
        if (isUserResponded) cancelResponse(event.id);
        else respondToEvent(event.id);
    };
    
    // Кнопка приглашения
    modal.querySelector('#inviteFriendBtn').onclick = () => toggleInviteDropdown(modal);
    
    // Кнопки для создателя
    if (isCreator) {
        modal.querySelector('#editEventBtn').onclick = () => editEvent(event);
        modal.querySelector('#deleteEventBtn').onclick = () => deleteEvent(event.id);
    }
}

function closeEventDetailsModal() {
    const modal = document.getElementById('eventDetailsModal');
    if (modal) {
        modal.classList.remove('active');
        const dropdown = modal.querySelector('#inviteFriendsDropdown');
        if (dropdown) dropdown.style.display = 'none';
    }
}

// ========== ЗАПИСЬ/ОТМЕНА ==========

async function respondToEvent(eventId) {
    try {
        const userId = localStorage.getItem("userId");
        await apiPost(`/api/events/${eventId}/respond?userId=${userId}`, {});
        alert("Вы записались на событие!");
        await window.openEventDetails(eventId);
        if (window.loadAllEventsData) await window.loadAllEventsData();
    } catch (error) {
        console.error("Ошибка отклика:", error);
        alert("Не удалось записаться на событие");
    }
}

async function cancelResponse(eventId) {
    try {
        const userId = localStorage.getItem("userId");
        await apiDelete(`/api/events/${eventId}/respond?userId=${userId}`);
        alert("Вы отменили запись на событие");
        await window.openEventDetails(eventId);
        if (window.loadAllEventsData) await window.loadAllEventsData();
    } catch (error) {
        console.error("Ошибка отмены записи:", error);
        alert("Не удалось отменить запись");
    }
}

// ========== ПРИГЛАШЕНИЕ ДРУЗЕЙ ==========

async function toggleInviteDropdown(modal) {
    const dropdown = modal.querySelector('#inviteFriendsDropdown');
    if (dropdown.style.display === "none") {
        await loadFriendsForInvite(modal);
        dropdown.style.display = "block";
    } else {
        dropdown.style.display = "none";
    }
}

async function loadFriendsForInvite(modal) {
    const container = modal.querySelector('#friendsListDropdown');
    const userId = localStorage.getItem("userId");
    
    try {
        const response = await fetch(`${API_URL}/api/friends/${userId}`);
        if (response.ok) {
            const friends = await response.json();
            friendsList = friends;
            renderFriendsForInvite(modal, friends);
            setupFriendSearch(modal, friends);
        }
    } catch (error) {
        console.error("Ошибка загрузки друзей:", error);
    }
}

function renderFriendsForInvite(modal, friends) {
    const container = modal.querySelector('#friendsListDropdown');
    if (!friends || friends.length === 0) {
        container.innerHTML = '<div style="padding: 20px; text-align: center; color: var(--muted);">У вас пока нет друзей</div>';
        return;
    }
    
    container.innerHTML = friends.map(friend => `
        <div class="friend-item" data-friend-id="${friend.id}">
            <div class="friend-info">
                <div class="friend-avatar-small">${getAvatarInitials(friend.name || friend.firstName)}</div>
                <span>${escapeHtml(friend.name || friend.firstName || 'Пользователь')}</span>
            </div>
            <button class="invite-friend-btn" onclick="window.inviteFriendToEvent(${friend.id})">📨</button>
        </div>
    `).join('');
}

function setupFriendSearch(modal, friends) {
    const searchInput = modal.querySelector('#friendSearchInput');
    searchInput.oninput = (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = friends.filter(f => 
            (f.name || f.firstName || '').toLowerCase().includes(query)
        );
        renderFriendsForInvite(modal, filtered);
    };
}

window.inviteFriendToEvent = async function(friendId) {
    if (!currentEvent) return;
    try {
        await apiPost(`/api/events/${currentEvent.id}/invite`, { friendId });
        alert("Приглашение отправлено!");
        const modal = document.getElementById('eventDetailsModal');
        const dropdown = modal.querySelector('#inviteFriendsDropdown');
        if (dropdown) dropdown.style.display = "none";
    } catch (error) {
        console.error("Ошибка приглашения:", error);
        alert("Не удалось отправить приглашение");
    }
};

// ========== РЕДАКТИРОВАНИЕ СОБЫТИЯ ==========

window.openEditEventModal = function(event) {
    let editModal = document.getElementById('editEventModal');
    
    if (!editModal) {
        editModal = createEditModalTemplate();
        document.body.appendChild(editModal);
        editModal.addEventListener('click', (e) => { if (e.target === editModal) closeEditEventModal(); });
        document.getElementById('closeEditModalBtn').onclick = closeEditEventModal;
        document.getElementById('saveEventEditBtn').onclick = () => saveEventEdit(event.id);
    }
    
    fillEditForm(event);
    editModal.classList.add('active');
};

function closeEditEventModal() {
    const modal = document.getElementById('editEventModal');
    if (modal) modal.classList.remove('active');
}

function createEditModalTemplate() {
    const modal = document.createElement('div');
    modal.id = 'editEventModal';
    modal.className = 'modal';
    
    modal.innerHTML = `
        <div class="modal-content">
            <button class="modal-close-btn" id="closeEditModalBtn">✕</button>
            <h2>Редактировать событие</h2>
            <input type="text" id="editEventTitle" placeholder="Название события">
            <select id="editEventType">
                <option value="Учёба">Учёба</option>
                <option value="Спорт">Спорт</option>
                <option value="Вечеринка">Вечеринка</option>
                <option value="Образование">Образование</option>
                <option value="Развлечения">Развлечения</option>
                <option value="Общение">Общение</option>
                <option value="Другое">Другое</option>
            </select>
            <input type="text" id="editEventLocation" placeholder="Место проведения">
            <input type="date" id="editEventDate">
            <input type="time" id="editEventTime">
            <textarea id="editEventDescription" placeholder="Описание события"></textarea>
            
            <div class="privacy-group">
                <label><input type="radio" name="editPrivacy" value="public" checked> Публичное</label>
                <label><input type="radio" name="editPrivacy" value="private"> Закрытое (только для друзей)</label>
            </div>
            
            <button class="upload-photo-btn" id="editUploadPhotoBtn">Загрузить новое фото</button>
            <input type="file" id="editPhotoInput" accept="image/*" style="display: none;">
            
            <div class="modal-buttons">
                <button class="cancel-btn" id="closeEditModalBtn">Отмена</button>
                <button class="submit-btn" id="saveEventEditBtn">Сохранить изменения</button>
            </div>
        </div>
    `;
    
    const uploadBtn = modal.querySelector('#editUploadPhotoBtn');
    const photoInput = modal.querySelector('#editPhotoInput');
    uploadBtn.onclick = () => photoInput.click();
    photoInput.onchange = (e) => {
        if (e.target.files[0]) {
            window.tempEventPhoto = e.target.files[0];
            uploadBtn.textContent = 'Фото выбрано ✅';
            setTimeout(() => { uploadBtn.textContent = 'Загрузить новое фото'; }, 2000);
        }
    };
    
    return modal;
}

function fillEditForm(event) {
    document.getElementById('editEventTitle').value = event.title || '';
    document.getElementById('editEventType').value = event.type || 'Учёба';
    document.getElementById('editEventLocation').value = event.location || '';
    document.getElementById('editEventDate').value = event.date || '';
    document.getElementById('editEventTime').value = event.time || '';
    document.getElementById('editEventDescription').value = event.description || '';
    
    const privacyValue = event.privacy || 'public';
    document.querySelectorAll('input[name="editPrivacy"]').forEach(radio => {
        radio.checked = radio.value === privacyValue;
    });
}

async function saveEventEdit(eventId) {
    const title = document.getElementById('editEventTitle').value.trim();
    const type = document.getElementById('editEventType').value;
    const location = document.getElementById('editEventLocation').value.trim();
    const date = document.getElementById('editEventDate').value;
    const time = document.getElementById('editEventTime').value;
    const description = document.getElementById('editEventDescription').value.trim();
    const privacy = document.querySelector('input[name="editPrivacy"]:checked')?.value || 'public';
    
    if (!title || !location || !date || !time) {
        alert('Пожалуйста, заполните все обязательные поля');
        return;
    }
    
    const formData = new FormData();
    formData.append('title', title);
    formData.append('type', type);
    formData.append('location', location);
    formData.append('date', date);
    formData.append('time', time);
    formData.append('description', description);
    formData.append('privacy', privacy);
    if (window.tempEventPhoto) formData.append('photo', window.tempEventPhoto);
    
    try {
        const response = await fetch(`${API_URL}/api/events/${eventId}`, { method: 'PUT', body: formData });
        if (!response.ok) throw new Error(await response.text());
        
        alert('Событие успешно обновлено!');
        closeEditEventModal();
        delete window.tempEventPhoto;
        if (window.loadAllEventsData) await window.loadAllEventsData();
        closeEventDetailsModal();
        setTimeout(() => window.openEventDetails(eventId), 300);
    } catch (error) {
        console.error('Ошибка обновления:', error);
        alert(error.message || 'Не удалось обновить событие');
    }
}

function editEvent(event) {
    closeEventDetailsModal();
    setTimeout(() => window.openEditEventModal(event), 200);
}

// ========== УДАЛЕНИЕ СОБЫТИЯ ==========

async function deleteEvent(eventId) {
    if (!confirm("⚠️ Вы уверены, что хотите удалить это событие?\n\nЭто действие нельзя отменить.")) return;
    
    try {
        const response = await fetch(`${API_URL}/api/events/${eventId}`, { method: 'DELETE' });
        if (!response.ok) throw new Error(await response.text());
        
        alert('Событие успешно удалено');
        closeEventDetailsModal();
        if (window.loadAllEventsData) await window.loadAllEventsData();
        else if (typeof loadAllEventsData === 'function') await loadAllEventsData();
        else window.location.reload();
    } catch (error) {
        console.error('Ошибка удаления:', error);
        alert(error.message || 'Не удалось удалить событие');
    }
}