// js/components.js - Web Components для header и sidebar

class HseHeader extends HTMLElement {
    constructor() {
        super();
        this.loadTemplate();
    }
    
    loadTemplate() {
        this.innerHTML = `
            <header class="topbar">
                <a class="brand" href="#" aria-label="HSE Connect">
                    <img class="brand-icon" src="../icons/logo.png" alt="logo" />
                    <img class="brand-text" src="../icons/logo_text_white_blue.png" alt="ВЫШКоннект" />
                </a>

                <div class="topbar-right">
                    <img class="mini-avatar" src="../stubs/photo_circle.svg" alt="Фото профиля" />
                    <span class="caret" aria-hidden="true"></span>
                </div>
            </header>
        `;
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