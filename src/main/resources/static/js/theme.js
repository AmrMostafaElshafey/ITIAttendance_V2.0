(function () {
    const body = document.body;
    const savedTheme = localStorage.getItem('iti-theme') || 'default';
    if (savedTheme === 'alt') {
        body.classList.add('theme-alt');
    }

    const toggle = document.createElement('button');
    toggle.className = 'theme-toggle';
    toggle.setAttribute('aria-label', 'تغيير الألوان');
    toggle.innerHTML = '<span>🎨</span><span id="theme-label">ألوان دافئة</span>';

    const updateLabel = () => {
        const label = document.getElementById('theme-label');
        if (!label) return;
        label.textContent = body.classList.contains('theme-alt') ? 'ألوان هادئة' : 'ألوان دافئة';
    };

    toggle.addEventListener('click', () => {
        body.classList.toggle('theme-alt');
        const isAlt = body.classList.contains('theme-alt');
        localStorage.setItem('iti-theme', isAlt ? 'alt' : 'default');
        updateLabel();
    });

    updateLabel();
    document.addEventListener('DOMContentLoaded', () => {
        document.body.appendChild(toggle);
        updateLabel();
    });
})();
