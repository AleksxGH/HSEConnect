// js/components.js - Web Components для header и sidebar

class HseHeader extends HTMLElement {
    constructor() {
        super();
        this.loadTemplate();
        this.initDropdown();
    }
    
    loadTemplate() {
        this.innerHTML = `
            <header class="topbar">
                <div class="brand" href="#" aria-label="HSE Connect">
                    <img class="brand-icon" src="../icons/logo.png" alt="logo" />
                    <img class="brand-text" src="../icons/logo_text_white_blue.png" alt="ВЫШКоннект" />
                </div>

                <div class="topbar-right">
                    <img class="mini-avatar" src="../stubs/photo_circle.svg" alt="Фото профиля" />
                    <img class="dropdown-icon" src="../icons/dropdown_icon.svg" alt="Меню" />
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
    
    initDropdown() {
        // Ждем загрузки DOM
        setTimeout(() => {
            const dropdownIcon = this.querySelector('.dropdown-icon');
            const dropdownMenu = this.querySelector('.dropdown-menu');
            
            if (dropdownIcon && dropdownMenu) {
                // Открытие/закрытие при клике на иконку
                dropdownIcon.addEventListener('click', (e) => {
                    e.stopPropagation();
                    dropdownMenu.classList.toggle('show');
                });
                
                // Закрытие при клике вне меню
                document.addEventListener('click', (e) => {
                    if (!dropdownIcon.contains(e.target) && !dropdownMenu.contains(e.target)) {
                        dropdownMenu.classList.remove('show');
                    }
                });
                
                // Обработчик кнопки "Выйти" - редирект без подтверждения
                const logoutBtn = this.querySelector('#logoutBtn');
                if (logoutBtn) {
                    logoutBtn.addEventListener('click', () => {
                        // Редирект на страницу авторизации
                        window.location.href = '../pages/auth.html';
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
    
    loadTemplate() {
        this.innerHTML = `
            <aside class="sidebar">
                <nav class="menu" aria-label="Боковое меню">
                    <a class="menu-item" href="../pages/profile.html" data-page="profile">
                        <img src="../icons/profile_icon.svg" alt="" aria-hidden="true" />
                        <span>Профиль</span>
                    </a>
                    <a class="menu-item" href="../index.html" data-page="home">
                        <img src="../icons/home_icon.svg" alt="" aria-hidden="true" />
                        <span>Главная</span>
                    </a>
                    <a class="menu-item" href="../pages/chat.html" data-page="chat">
                        <img src="../icons/chat_icon.svg" alt="" aria-hidden="true" />
                        <span>Сообщения</span>
                    </a>
                    <a class="menu-item" href="#" data-page="notifications">
                        <img src="../icons/notifications_icon.svg" alt="" aria-hidden="true" />
                        <span>Уведомления</span>
                    </a>
                    <a class="menu-item" href="#" data-page="friends">
                        <img src="../icons/friends_icon.svg" alt="" aria-hidden="true" />
                        <span>Друзья</span>
                    </a>
                </nav>
            </aside>
        `;
    }
    
    highlightActivePage() {
        // Определяем текущую страницу по URL
        const currentPath = window.location.pathname;
        let currentPage = '';
        
        if (currentPath.includes('profile.html')) currentPage = 'profile';
        else if (currentPath.includes('chat.html')) currentPage = 'chat';
        else if (currentPath === '/' || currentPath.includes('index.html')) currentPage = 'home';
        
        // Добавляем класс active к соответствующей ссылке
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

// Регистрируем компоненты
customElements.define('hse-header', HseHeader);
customElements.define('hse-sidebar', HseSidebar);