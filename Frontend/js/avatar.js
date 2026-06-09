// avatar.js - универсальный модуль для работы с аватарками

// Функция для получения инициалов из имени и фамилии (2 буквы)
function getInitials(firstName, lastName) {
    const first = firstName ? firstName.charAt(0).toUpperCase() : '';
    const last = lastName ? lastName.charAt(0).toUpperCase() : '';
    
    if (first && last) {
        return `${first}${last}`;
    }
    if (first) {
        return first;
    }
    if (last) {
        return last;
    }
    return '?';
}

// Функция для генерации цвета на основе строки
function getColorFromString(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    
    const colors = [
        '#4b59ff', '#6f42c1',
        '#20c997','#17a2b8'
    ];
    
    return colors[Math.abs(hash) % colors.length];
}

// Функция для создания элемента аватарки с инициалами (только с классами, без инлайн-стилей)
function createInitialsAvatar(firstName, lastName) {
    const initials = getInitials(firstName, lastName);
    const bgColor = getColorFromString(`${firstName || ''}${lastName || ''}`);
    
    const avatarDiv = document.createElement('div');
    avatarDiv.className = 'avatar-initials';
    avatarDiv.setAttribute('data-initials', initials);
    avatarDiv.style.backgroundColor = bgColor;
    avatarDiv.textContent = initials;
    
    return avatarDiv;
}

// Функция для проверки, есть ли у пользователя аватарка
async function hasUserAvatar(userId) {
    try {
        const response = await fetch(`${API_URL}/api/users/${userId}/avatar`);
        return response.ok;
    } catch (error) {
        console.error('Ошибка проверки аватарки:', error);
        return false;
    }
}

// Функция для загрузки аватарки пользователя
async function loadUserAvatar(userId) {
    try {
        const response = await fetch(`${API_URL}/api/users/${userId}/avatar`);
        if (response.ok) {
            const blob = await response.blob();
            return URL.createObjectURL(blob);
        }
        return null;
    } catch (error) {
        console.error('Ошибка загрузки аватарки:', error);
        return null;
    }
}

// Функция для обновления аватарки
async function updateUserAvatar(userId, file) {
    const formData = new FormData();
    formData.append('avatar', file);
    
    try {
        const response = await fetch(`${API_URL}/api/users/${userId}/avatar`, {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) {
            throw new Error('Ошибка загрузки аватарки');
        }
        
        return await response.json();
    } catch (error) {
        console.error(error);
        throw error;
    }
}

// Функция для удаления аватарки
async function deleteUserAvatar(userId) {
    try {
        const response = await fetch(`${API_URL}/api/users/${userId}/avatar`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Ошибка удаления аватарки');
        }
        
        return true;
    } catch (error) {
        console.error(error);
        throw error;
    }
}

// Функция для открытия выбора файла
function openFileSelector(userId, onAvatarUpdate) {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    fileInput.style.display = 'none';
    
    fileInput.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) {
                alert('Файл слишком большой. Максимальный размер 5MB');
                return;
            }
            
            if (!file.type.startsWith('image/')) {
                alert('Пожалуйста, выберите изображение');
                return;
            }
            
            try {
                await updateUserAvatar(userId, file);
                if (onAvatarUpdate) await onAvatarUpdate();
                alert('Аватарка успешно обновлена');
            } catch (error) {
                alert('Ошибка при загрузке аватарки');
            }
        }
        fileInput.remove();
    });
    
    document.body.appendChild(fileInput);
    fileInput.click();
}

// Модальное окно для управления аватаркой
function showAvatarModal(currentAvatarUrl, userId, firstName, lastName, onAvatarUpdate) {
    const modal = document.createElement('div');
    modal.className = 'modal avatar-modal';
    modal.style.display = 'flex';
    
    modal.innerHTML = `
        <div class="modal-content avatar-modal-content">
            <h2>Фото профиля</h2>
            
            <div class="avatar-preview-container">
                <div class="avatar-preview" id="avatarPreview">
                    ${currentAvatarUrl ? 
                        `<img src="${currentAvatarUrl}" alt="Аватарка" style="width: 150px; height: 150px; border-radius: 50%; object-fit: cover;">` :
                        `<div class="avatar-initials-preview" style="width: 150px; height: 150px; border-radius: 50%; background-color: ${getColorFromString(firstName + lastName)}; display: flex; align-items: center; justify-content: center; color: white; font-size: 48px; font-weight: 600;">${getInitials(firstName, lastName)}</div>`
                    }
                </div>
            </div>
            
            <div class="avatar-actions" style="display: flex; justify-content: center; gap: 16px; margin-top: 20px;">
                <button class="avatar-action-btn" id="changeAvatarBtn">
                    <span>📷 Заменить</span>
                </button>
                ${currentAvatarUrl ? `
                    <button class="avatar-action-btn delete" id="deleteAvatarBtn">
                        <span>🗑️ Удалить</span>
                    </button>
                ` : ''}
            </div>
            
            <div class="modal-buttons" style="margin-top: 20px;">
                <button class="cancel-btn" id="closeAvatarModalBtn">Закрыть</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    modal.classList.add('active');
    
    const changeBtn = modal.querySelector('#changeAvatarBtn');
    const deleteBtn = modal.querySelector('#deleteAvatarBtn');
    const closeBtn = modal.querySelector('#closeAvatarModalBtn');
    
    changeBtn?.addEventListener('click', () => {
        modal.remove();
        openFileSelector(userId, onAvatarUpdate);
    });
    
    deleteBtn?.addEventListener('click', async () => {
        if (confirm('Удалить фото профиля?')) {
            try {
                await deleteUserAvatar(userId);
                modal.remove();
                if (onAvatarUpdate) await onAvatarUpdate();
                alert('Аватарка удалена');
            } catch (error) {
                alert('Ошибка при удалении аватарки');
            }
        }
    });
    
    closeBtn?.addEventListener('click', () => {
        modal.remove();
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.remove();
        }
    });
}

// Основная функция отображения аватарки
async function renderAvatar(container, userId, firstName, lastName) {
    if (!container) return;
    
    container.innerHTML = '';
    container.style.cursor = 'pointer';
    
    const hasAvatar = await hasUserAvatar(userId);
    
    if (hasAvatar) {
        const avatarUrl = await loadUserAvatar(userId);
        const img = document.createElement('img');
        img.className = 'avatar';
        img.src = avatarUrl;
        img.alt = 'Фото профиля';
        container.appendChild(img);
        
        container.onclick = async () => {
            const currentAvatarUrl = await loadUserAvatar(userId);
            showAvatarModal(currentAvatarUrl, userId, firstName, lastName, async () => {
                await renderAvatar(container, userId, firstName, lastName);
            });
        };
    } else {
        const initialsAvatar = createInitialsAvatar(firstName, lastName);
        container.appendChild(initialsAvatar);
        
        container.onclick = () => {
            openFileSelector(userId, async () => {
                await renderAvatar(container, userId, firstName, lastName);
            });
        };
    }
}

async function renderMiniAvatar(container, userId, firstName, lastName) {
    if (!container) return;
    
    container.innerHTML = '';
    
    const hasAvatar = await hasUserAvatar(userId);
    
    if (hasAvatar) {
        const avatarUrl = await loadUserAvatar(userId);
        const img = document.createElement('img');
        img.className = 'mini-avatar';
        img.src = avatarUrl;
        img.alt = 'Фото профиля';
        img.style.width = '32px';
        img.style.height = '32px';
        img.style.borderRadius = '50%';
        img.style.objectFit = 'cover';
        container.appendChild(img);
    } else {
        const initials = getInitials(firstName, lastName);
        const bgColor = getColorFromString(`${firstName || ''}${lastName || ''}`);
        
        const initialsDiv = document.createElement('div');
        initialsDiv.className = 'mini-avatar-initials';
        initialsDiv.style.width = '32px';
        initialsDiv.style.height = '32px';
        initialsDiv.style.borderRadius = '50%';
        initialsDiv.style.backgroundColor = bgColor;
        initialsDiv.style.display = 'flex';
        initialsDiv.style.alignItems = 'center';
        initialsDiv.style.justifyContent = 'center';
        initialsDiv.style.color = 'white';
        initialsDiv.style.fontSize = '12px';
        initialsDiv.style.fontWeight = '600';
        initialsDiv.style.fontFamily = 'Montserrat, sans-serif';
        initialsDiv.textContent = initials;
        container.appendChild(initialsDiv);
    }
}

// Экспортируем функции
window.avatarAPI = {
    hasUserAvatar,
    loadUserAvatar,
    updateUserAvatar,
    deleteUserAvatar,
    renderAvatar,
    showAvatarModal,
    createInitialsAvatar,
    getInitials,
    openFileSelector,
    renderMiniAvatar
};