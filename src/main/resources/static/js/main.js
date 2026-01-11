// --- static/js/main.js ---
if (!window.API_BASE_URL) {
    window.API_BASE_URL = window.location.origin + '/api';
}

if (!window.BASE_URL) {
    window.BASE_URL = window.location.origin;
}
const DEFAULT_AVATAR = "https://i.pravatar.cc/150?img=11";

const contentArea = document.getElementById('content-area');
const pageTitle = document.getElementById('pageTitle');

// --- 1. BIẾN TOÀN CỤC CHO PLAYER ---
let currentPlaylist = [];
let currentIndex = 0;
let isPlaying = false;

// Lấy các element của Player
const musicPlayerBar = document.getElementById('musicPlayer');
const audio = document.getElementById('mainAudio');
const playerTitle = document.getElementById('playerTitle');
const playerArtist = document.getElementById('playerArtist');
const playerThumb = document.getElementById('playerThumb');
const btnPlayPause = document.getElementById('btnPlayPause');
const iconPlay = document.getElementById('iconPlay');
const progress = document.getElementById('progress');
const currentTimeEl = document.getElementById('currentTime');
const durationTimeEl = document.getElementById('durationTime');
const btnNext = document.getElementById('btnNext');
const btnPrev = document.getElementById('btnPrev');
const volumeSlider = document.getElementById('volume');
const volumeIcon = document.getElementById('volumeIcon');

// --- 2. KHỞI CHẠY LẦN ĐẦU ---
document.addEventListener("DOMContentLoaded", () => {
    // 1. Kiểm tra User đã đăng nhập chưa để hiển thị Header
    checkLoginStatus();

    // 2. Mặc định vào trang Home
    loadPage('home');

    // 3. Cài đặt sự kiện Player
    setupPlayerEvents();

    renderNotifications();

    // Dong khi click ra ngoai
    window.addEventListener('click', (e) =>{
        if (!e.target.closest('.notification-wrapper')){
            const dropdown = document.getElementById('notif-dropdown');
            if (dropdown) dropdown.classList.remove('show');
        }
    })
});

// --- 3. HÀM CHUYỂN TRANG (SPA ROUTER) ---
function loadPage(pageName) {
    const protectedPages = ['library', 'upload', 'profile'];

    // Kiểm tra xem trang người dùng muốn vào có nằm trong danh sách bảo vệ không
    if (protectedPages.includes(pageName)) {
        const user = JSON.parse(localStorage.getItem('currentUser'));

        // Nếu CHƯA ĐĂNG NHẬP
        if (!user) {
            window.location.href = 'login.html';
            return;
        }
    }
    const fileName = `html/${pageName}.html`;

    fetch(fileName)
        .then(response => {
            if (!response.ok) throw new Error("File not found");
            return response.text();
        })
        .then(html => {
            // A. Nhét HTML vào khung
            contentArea.innerHTML = html;

            // B. Update Menu Active
            updateMenu(pageName);

            // C. RESET Header Text
            pageTitle.classList.remove('welcome-text');
            pageTitle.style.fontFamily = "";

            // D. GỌI HÀM KHỞI TẠO RIÊNG TỪNG TRANG
            if (pageName === 'library') {
                pageTitle.innerText = "MY LIBRARY";
                if (typeof initLibraryPage === "function") initLibraryPage();
            }
            else if (pageName === 'upload') {
                pageTitle.innerText = "UPLOAD ALBUM";
                if (typeof initUploadPage === "function") initUploadPage();
            }
            else if (pageName === 'home') {
                pageTitle.innerText = "WELCOME BACK";
                pageTitle.classList.add('welcome-text');
                if (typeof initHomePage === "function") initHomePage();
            }
            else if (pageName === 'profile') {
                pageTitle.innerText = "EDIT PROFILE";
                // Đóng dropdown menu nếu đang mở
                const dropdown = document.getElementById('userDropdown');
                if(dropdown) dropdown.classList.remove('show');

                if (typeof initProfilePage === "function") initProfilePage();
            }
        })
        .catch(err => console.error('Lỗi tải trang:', err));
}

function updateMenu(pageName) {
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
        const onclickAttr = item.getAttribute('onclick');
        if (onclickAttr && onclickAttr.includes(`'${pageName}'`)) {
            item.classList.add('active');
        }
    });
}

// --- 4. LOGIC AUTH (KIỂM TRA TRẠNG THÁI ĐĂNG NHẬP) ---

function checkLoginStatus() {
    const guestNav = document.getElementById('guest-nav');
    const userNav = document.getElementById('user-nav');
    const userNameDisplay = document.getElementById('userNameDisplay');
    const userAvatar = document.getElementById('userAvatar');

    // Lấy user từ localStorage (được lưu từ trang login.html)
    const user = JSON.parse(localStorage.getItem('currentUser'));

    if (user) {
        // ĐÃ ĐĂNG NHẬP: Ẩn nút Login, Hiện Avatar
        if(guestNav) guestNav.style.display = 'none';
        if(userNav) userNav.style.display = 'flex';

        if(userNameDisplay) userNameDisplay.innerText = user.username || "User";
        if(userAvatar) userAvatar.src = user.avatar ? user.avatar : DEFAULT_AVATAR;
    } else {
        // CHƯA ĐĂNG NHẬP: Hiện nút Login
        if(guestNav) guestNav.style.display = 'flex';
        if(userNav) userNav.style.display = 'none';
    }
}

function toggleUserDropdown() {
    const dropdown = document.getElementById('userDropdown');
    if (dropdown) dropdown.classList.toggle('show');
}

function handleLogout(event) {
    if(event) event.stopPropagation();

    // Xóa session
    localStorage.removeItem('currentUser');

    // Đóng menu
    const dropdown = document.getElementById('userDropdown');
    if(dropdown) dropdown.classList.remove('show');

    // Cập nhật lại giao diện Header
    checkLoginStatus();

    // Quay về trang chủ (hoặc chuyển sang login.html nếu muốn bắt buộc đăng nhập)
    window.location.href = 'login.html';

    alert("Đã đăng xuất thành công!");
}


// --- 5. LOGIC PLAYER ---

function playSongByIndex(index) {
    currentIndex = index;
    const song = currentPlaylist[index];
    if (!song) return;

    // Hiện thanh player nếu nó đang ẩn (mặc định transform translate)
    if (musicPlayerBar) {
        musicPlayerBar.classList.add('active');
        // Thêm padding cho nội dung chính để không bị player che khuất
        const mainContent = document.querySelector('.main-content');
        if(mainContent) mainContent.classList.add('has-player');
    }

    playerTitle.innerText = song.title;
    playerArtist.innerText = song.artist;
    let imageUrl = song.coverImage ? `${window.BASE_URL}${song.coverImage}` : 'https://picsum.photos/50/50';
    playerThumb.style.backgroundImage = `url('${imageUrl}')`;

    if (song.audioUrl) {
        audio.src = `${window.BASE_URL}${song.audioUrl}`;
        audio.play()
            .then(() => {
                isPlaying = true;
                updatePlayIcon();
            })
            .catch(err => console.error("Lỗi phát nhạc:", err));
    } else {
        alert("Không tìm thấy file nhạc!");
    }
}

function updatePlayIcon() {
    if (isPlaying) {
        iconPlay.classList.remove('fa-play');
        iconPlay.classList.add('fa-pause');
    } else {
        iconPlay.classList.remove('fa-pause');
        iconPlay.classList.add('fa-play');
    }
}

function setupPlayerEvents() {
    if (btnPlayPause) {
        btnPlayPause.addEventListener('click', () => {
            if (isPlaying) {
                audio.pause();
                isPlaying = false;
            } else {
                if (!audio.src && currentPlaylist.length > 0) {
                    playSongByIndex(0);
                } else {
                    audio.play();
                    isPlaying = true;
                }
            }
            updatePlayIcon();
        });
    }

    audio.addEventListener('timeupdate', (e) => {
        const { duration, currentTime } = e.srcElement;
        if (duration) {
            const progressPercent = (currentTime / duration) * 100;
            if (progress) progress.value = progressPercent;
            if (currentTimeEl) currentTimeEl.innerText = formatTime(currentTime);
            if (durationTimeEl) durationTimeEl.innerText = formatTime(duration);
        }
    });

    if (progress) {
        progress.addEventListener('input', () => {
            const duration = audio.duration;
            audio.currentTime = (progress.value * duration) / 100;
        });
    }

    audio.addEventListener('ended', nextSong);
    if (btnNext) btnNext.addEventListener('click', nextSong);
    if (btnPrev) btnPrev.addEventListener('click', prevSong);

    if (volumeSlider) {
        volumeSlider.addEventListener('input', (e) => {
            const val = e.target.value;
            audio.volume = val / 100;
            updateVolumeIcon(val);
        });
    }
}

function nextSong() {
    let nextIndex = currentIndex + 1;
    if (nextIndex >= currentPlaylist.length) nextIndex = 0;
    playSongByIndex(nextIndex);
}

function prevSong() {
    let prevIndex = currentIndex - 1;
    if (prevIndex < 0) prevIndex = currentPlaylist.length - 1;
    playSongByIndex(prevIndex);
}

function updateVolumeIcon(val) {
    if (volumeIcon) {
        if (val == 0) volumeIcon.className = 'fa-solid fa-volume-xmark';
        else if (val < 50) volumeIcon.className = 'fa-solid fa-volume-low';
        else volumeIcon.className = 'fa-solid fa-volume-high';
    }
}

function formatTime(seconds) {
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60);
    return `${min}:${sec < 10 ? '0' + sec : sec}`;
}

// --- 6. LOGIC NOTIFICATION ---
// Ham them thong bao moi
function addNotification(message){
    // Lay danh sach cu tu LocalStorage
    let notifications = JSON.parse(localStorage.getItem('user_notifications')) || [];

    // Tao object thong bao moi
    const newNotif = {
        id: Date.now(),
        message: message,
        time: new Date().toLocaleString(),
        read: false
    };

    // Them vao dau danh sach
    notifications.unshift(newNotif);

    // Gioi han 20 thong bao
    if (notifications.length > 20) notifications.pop();

    //Luu lai
    localStorage.setItem('user_notifications', JSON.stringify(notifications));

    // Cap nhat giao dien
    renderNotifications();
}

// Ham hien thi thong bao ra man hinh
function renderNotifications(){
    const notifications = JSON.parse(localStorage.getItem('user_notifications')) || [];
    const listEl = document.getElementById('notif-list');
    const badgeEl = document.getElementById('notif-badge');

    if (!listEl || !badgeEl) return;

    // Dem so tin chua doc
    const unreadCount = notifications.filter(n => !n.read).length;

    // Xu ly Badge (Cham do)
    if (unreadCount > 0){
        badgeEl.innerText = unreadCount;
        badgeEl.style.display = 'block'
    }else{
        badgeEl.style.display = 'none';
    }

    // Xu ly danh sach
    if (notifications.length === 0){
        listEl.innerHTML = '<div class="empty-notif">No notification</div>';
        return;
    }

    let html = '';
    notifications.forEach(notif =>{
        html += `
            <div class="notif-item ${notif.read ? '' : 'unread'}">
                <i class="fa-solid fa-circle-check"></i>
                <div>
                    <div style="font-size: 13px;">${notif.message}</div>
                    <div style="font-size: 11px; color: #777; margin-top: 2px;">${notif.time}</div>
                </div>
            </div>
        
        `;
    });
    listEl.innerHTML = html;
}

// Ham Bat/Tat bang thong bao
function toggleNotification(){
    const dropdown = document.getElementById('notif-dropdown');
    if (!dropdown) return;

    const isClosed = !dropdown.classList.contains('show');

    // Dong tat ca dropwdown khac
    document.querySelectorAll('.show').forEach(el => el.classList.remove('show'));

    if (isClosed){
        dropdown.classList.add('show');
        markAllAsRead(); // Mo ra la da doc
    }else{
        dropdown.classList.remove('show');
    }
}

// Ham danh dau tat cac la da doc (tat cham do)
function markAllAsRead(){
    let notifications = JSON.parse(localStorage.getItem('user_notifications')) || [];
    notifications.forEach(n => n.read = true);
    localStorage.setItem('user_notifications', JSON.stringify(notifications));
    renderNotifications(); // Ve lai de mat cham do
}

// Ham xoa het thong bao
function clearAllNotifications(){
    localStorage.removeItem('user_notifications');
    renderNotifications();
}


// Đóng dropdown khi click ra ngoài
window.addEventListener('click', function(e) {
    const wrapper = document.querySelector('.user-dropdown-wrapper');
    const dropdown = document.getElementById('userDropdown');
    if (wrapper && dropdown && !wrapper.contains(e.target)) {
        dropdown.classList.remove('show');
    }
});