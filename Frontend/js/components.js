// js/components.js - Web Components для header и sidebar

class HseHeader extends HTMLElement {
  constructor() {
    super();
    this.loadTemplate();
    this.initDropdown();
    this.loadUserAvatar();
  }

  getBasePath() {
    const currentPath = window.location.pathname;
    return currentPath.includes('/pages/') ? '../' : '';
  }

  loadTemplate() {
    const basePath = this.getBasePath();
    this.innerHTML = `
            <header class="topbar">
                <a class="brand" href="${basePath}index.html" aria-label="HSE Connect">
                    <img class="brand-icon" src="${basePath}icons/logo.png" alt="logo" />
                    <img class="brand-text" src="${basePath}icons/logo_text_white_blue.png" alt="ВЫШКоннект" />
                </a>

                <div class="topbar-right">
                    <div class="user-menu-trigger" id="userMenuTrigger">
                        <div class="avatar-container" id="headerAvatar">
                            <img class="mini-avatar" src="${
        basePath}stubs/photo_circle.jpg" alt="Фото профиля" style="display: none;" />
                            <div class="mini-avatar-initials" style="display: none;"></div>
                        </div>
                        <img class="dropdown-icon" src="${basePath}icons/dropdown_icon.svg" alt="Меню" />
                    </div>
                    <div class="dropdown-menu" id="userDropdown">
                        <div class="dropdown-item" id="logoutBtn">
                            <svg class="dropdown-icon-svg" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M16 17L21 12L16 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span>Выйти</span>
                        </div>
                    </div>
                </div>
            </header>
        `;
  }

  async loadUserAvatar() {
    // Ждем загрузки avatarAPI
    if (!window.avatarAPI) {
      setTimeout(() => this.loadUserAvatar(), 100);
      return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId)
      return;

    try {
      // Загружаем данные пользователя для получения имени
      const response = await fetch(`${API_URL}/api/profile/${userId}`);
      if (response.ok) {
        const profile = await response.json();
        const firstName = profile.firstName || '';
        const lastName = profile.lastName || '';

        const avatarContainer = this.querySelector('#headerAvatar');
        if (avatarContainer) {
          await window.avatarAPI.renderMiniAvatar(avatarContainer, userId, firstName, lastName);
        }
      }
    } catch (error) {
      console.error('Ошибка загрузки аватарки в header:', error);
    }
  }

  initDropdown() {
    setTimeout(() => {
      const userMenuTrigger = this.querySelector('#userMenuTrigger');
      const dropdownMenu = this.querySelector('#userDropdown');

      if (userMenuTrigger && dropdownMenu) {
        // Клик по триггеру (аватарка + иконка) открывает/закрывает меню
        userMenuTrigger.addEventListener('click', (e) => {
          e.stopPropagation();
          dropdownMenu.classList.toggle('show');
        });

        // Закрытие при клике вне меню
        document.addEventListener('click', (e) => {
          if (userMenuTrigger && dropdownMenu && !userMenuTrigger.contains(e.target) &&
              !dropdownMenu.contains(e.target)) {
            dropdownMenu.classList.remove('show');
          }
        });

        // Кнопка выхода
        const logoutBtn = this.querySelector('#logoutBtn');
        if (logoutBtn) {
          logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('userId');
            sessionStorage.clear();

            window.location.replace('/pages/auth.html');
          });
        }
      }
    }, 0);
  }
}

class HseSidebar extends HTMLElement {
  constructor() {
    super();
    this.loadTemplate();
    this.highlightActivePage();
  }

  getBasePath() {
    const currentPath = window.location.pathname;
    if (currentPath.includes('/pages/')) {
      return '../';
    }
    return '';
  }

  loadTemplate() {
    const basePath = this.getBasePath();
    this.innerHTML = `
            <aside class="sidebar">
                <nav class="menu" aria-label="Боковое меню">
                    <a class="menu-item" href="${basePath}pages/profile.html" data-page="profile">
                        <img src="${basePath}icons/profile_icon.svg" alt="" aria-hidden="true" />
                        <span>Профиль</span>
                    </a>
                    <a class="menu-item" href="${basePath}index.html" data-page="home">
                        <img src="${basePath}icons/home_icon.svg" alt="" aria-hidden="true" />
                        <span>Главная</span>
                    </a>
                    <a class="menu-item" href="${basePath}pages/chat.html" data-page="chat">
                        <img src="${basePath}icons/chat_icon.svg" alt="" aria-hidden="true" />
                        <span>Сообщения</span>
                    </a>
                    <a class="menu-item" href="${basePath}pages/notifications.html" data-page="notifications">
                        <img src="${basePath}icons/notifications_icon.svg" alt="" aria-hidden="true" />
                        <span>Уведомления</span>
                    </a>
                    <a class="menu-item" href="${basePath}pages/friends.html" data-page="friends">
                        <img src="${basePath}icons/friends_icon.svg" alt="" aria-hidden="true" />
                        <span>Связи</span>
                    </a>
                </nav>
            </aside>
        `;
  }

  highlightActivePage() {
    const currentPath = window.location.pathname;
    let currentPage = '';

    if (currentPath.includes('profile.html'))
      currentPage = 'profile';
    else if (currentPath.includes('chat.html'))
      currentPage = 'chat';
    else if (currentPath === '/' || currentPath.includes('index.html'))
      currentPage = 'home';
    else if (currentPath.includes('friends.html'))
      currentPage = 'friends';
    else if (currentPath.includes('notifications.html'))
      currentPage = 'notifications';

    setTimeout(() => {
      const links = this.querySelectorAll('.menu-item');
      links.forEach(link => {
        const pageAttr = link.getAttribute('data-page');
        if (pageAttr === currentPage) {
          link.classList.add('active');
        } else {
          link.classList.remove('active');
        }
      });
    }, 0);
  }
}

customElements.define('hse-header', HseHeader);
customElements.define('hse-sidebar', HseSidebar);