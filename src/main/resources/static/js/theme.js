(function () {
    const themes = [
        { key: 'warm', className: '', label: 'أحمر / رمادي' },
        { key: 'calm', className: 'theme-calm', label: 'أزرق هادئ' },
        { key: 'oasis', className: 'theme-oasis', label: 'نعناع ورملي' }
    ];

    const body = document.body;
    let currentIndex = themes.findIndex(t => t.key === (localStorage.getItem('iti-theme') || 'warm'));
    if (currentIndex < 0) currentIndex = 0;

    const applyTheme = (index) => {
        themes
            .map(t => t.className)
            .filter(Boolean)
            .forEach(cls => body.classList.remove(cls));

        currentIndex = index;
        const cls = themes[currentIndex].className;
        if (cls) body.classList.add(cls);
        document.documentElement.setAttribute('data-theme', themes[currentIndex].key);
        localStorage.setItem('iti-theme', themes[currentIndex].key);
        updateLabel();
    };

    const toggle = document.createElement('button');
    toggle.className = 'theme-toggle';
    toggle.setAttribute('aria-label', 'تغيير الألوان');
    toggle.innerHTML = '<span>🎨</span><span id="theme-label"></span>';

    const updateLabel = () => {
        const label = document.getElementById('theme-label');
        if (!label) return;
        label.textContent = `سمة: ${themes[currentIndex].label}`;
    };

    toggle.addEventListener('click', () => {
        const next = (currentIndex + 1) % themes.length;
        applyTheme(next);
    });

    document.addEventListener('DOMContentLoaded', () => {
        document.body.appendChild(toggle);
        applyTheme(currentIndex);
    });
})();

function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (!sidebar) return;
    sidebar.classList.toggle('sidebar-collapsed');
}
