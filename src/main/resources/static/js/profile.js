// --- static/js/profile.js ---

// 1. Cấu hình API
if (!window.API_BASE_URL) {
    window.API_BASE_URL = window.location.origin + '/api';
}
const BASE_URL = window.location.origin;
// 2. Chạy khi trang load
document.addEventListener("DOMContentLoaded", () => {
    // Lấy user từ localStorage
    const user = JSON.parse(localStorage.getItem('currentUser'));

    // Nếu chưa đăng nhập -> Đuổi về login
    if (!user) {
        window.location.href = 'login.html';
        return;
    }

    // Điền dữ liệu vào form
    const usernameInput = document.getElementById('profileUsername');
    if (usernameInput) {
        // Nếu username chưa có (null), fallback tạm về email để họ có cái mà sửa
        usernameInput.value = user.username || user.email || "";
    }

    // 2. Ô Email (Chỉ đọc): Lấy user.email
    const emailInput = document.getElementById('profileEmail');
    if (emailInput) {
        emailInput.value = user.email || "";
    }

    // Hiển thị avatar hiện tại (nếu có)
    const avatarImg = document.getElementById('profileAvatar');
    if (avatarImg && user.avatar) {
        let avatarSrc = user.avatar;
        if (!avatarSrc.startsWith('http')) {
            avatarSrc = `${BASE_URL}${user.avatar}`;
        }
        avatarImg.src = avatarSrc;
    }
});

// 3. Hàm xem trước ảnh khi vừa chọn (Preview)
function previewAvatar(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('profileAvatar').src = e.target.result;
        }
        reader.readAsDataURL(input.files[0]);
    }
}

// 4. Xử lý Lưu thay đổi (QUAN TRỌNG: Đã sửa sang FormData)
async function handleSaveProfile(event) {
    event.preventDefault();

    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser || !currentUser.id) {
        alert("Lỗi session! Vui lòng đăng nhập lại.");
        return;
    }

    // Lấy dữ liệu từ form
    const newUsername = document.getElementById('profileUsername').value;
    const avatarInput = document.getElementById('avatarInput');
    const avatarFile = avatarInput.files[0]; // File ảnh thực sự

    // --- TẠO FORM DATA (Thay vì JSON) ---
    const formData = new FormData();
    formData.append('username', newUsername);

    // Chỉ gửi avatar nếu người dùng có chọn ảnh mới
    if (avatarFile) {
        formData.append('avatar', avatarFile);
    }

    // Gọi API Backend
    try {
        const response = await fetch(`${window.API_BASE_URL}/users/${currentUser.id}`, {
            method: 'PUT',
            body: formData,
            headers: {'Authorization' : `Bearer ${currentUser.token}`,}
            // LƯU Ý: Khi dùng FormData, KHÔNG được set 'Content-Type': 'application/json'
            // Browser sẽ tự động set 'multipart/form-data'
        });

        if (response.ok) {
            // 1. Backend trả về thông tin user mới nhất
            const updatedUser = await response.json();

            // 2. Cập nhật lại localStorage
            currentUser.username = updatedUser.username;

            // Nếu có avatar mới thì cập nhật
            if (updatedUser.avatar) {
                currentUser.avatar = updatedUser.avatar;
            }

            localStorage.setItem('currentUser', JSON.stringify(currentUser));

            alert("Cập nhật thành công!");
            window.location.href = 'index.html'; // Chỉ chuyển trang khi thành công
        } else {
            // Nếu lỗi, hiện tin nhắn từ Backend gửi về
            const errorText = await response.text();
            throw new Error(errorText);
        }
    } catch (e) {
        console.error(e);
        alert("Lỗi cập nhật: " + e.message);
        // KHÔNG chuyển trang ở đây để người dùng biết lỗi
    }
}

function goBack() {
    window.location.href = 'index.html';
}