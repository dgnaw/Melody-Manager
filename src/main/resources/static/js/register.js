// --- static/js/register.js ---
if (typeof API_BASE_URL === 'undefined') {
    var API_BASE_URL = 'http://localhost:8080/api';
}
// CLIENT ID (Phải giống bên login.js)
const GOOGLE_CLIENT_ID_REG = "346688855682-03o2g4bd3dc4c2b2b4sl6s6vakeiukbv.apps.googleusercontent.com";

// 1. Tự động khởi tạo nút Google khi trang register.html tải xong
window.onload = function() {
    initGoogleRegisterButton();
};

function initGoogleRegisterButton() {
    // Kiểm tra xem thư viện Google đã tải xong chưa
    if (typeof google === 'undefined') {
        setTimeout(initGoogleRegisterButton, 500); // Thử lại sau 0.5s
        return;
    }

    try {
        google.accounts.id.initialize({
            client_id: GOOGLE_CLIENT_ID_REG,
            callback: handleGoogleRegisterResponse
        });

        const container = document.getElementById("google-register-btn-container");
        if (container) {
            google.accounts.id.renderButton(
                container,
                {
                    theme: "outline",
                    size: "large",
                    width: "320",
                    text: "signup_with" // "Sign up with Google"
                }
            );
        }
    } catch (e) {
        console.error("Lỗi Google Button Register:", e);
    }
}


// 3. Xử lý Form đăng ký thường (Email Submit)
async function handleRegister(event) {
    event.preventDefault(); // Chặn reload trang

    const fullName = document.getElementById('regFullName').value;
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;

    if (!fullName || !username || !email || !password){
        alert("Vui lòng nhập đầy đủ thông tin!");
        return;
    }
    try{
        const response = await fetch(`${API_BASE_URL}/auth/register`,{
            method: "POST",
            headers:{
                'Content-Type' : 'application/json'
            },
            body : JSON.stringify({
                fullName: fullName,
                username: username,
                email: email,
                password: password
            })
        });
        if (response.ok){
            alert("Đăng ký thành công! Vui lòng đăng nhập.");
            window.location.href = "login.html";
        }else{
            const errorText = await response.text();
            alert("Đăng ký thất bại: " + errorText);
        }
    }catch (error){
        console.error("Lỗi kết nối: ",error);
        alert("Không thể kết nối tới Server!");
    }

}

// ============================================================
// 4. XỬ LÝ ĐĂNG KÝ GOOGLE (Gửi về Backend)
// ============================================================
async function handleGoogleRegisterResponse(response) {
    // A. Giải mã token
    const userPayload = decodeJwtResponse(response.credential);

    // B. Chuẩn bị dữ liệu gửi về Server
    const googleData = {
        email: userPayload.email,
        fullName: userPayload.name,
        avatar: userPayload.picture
    };

    try {
        // C. Gọi API Backend: /api/auth/google
        // (API này sẽ tự động Tạo mới nếu chưa có, hoặc Đăng nhập nếu có rồi)
        const res = await fetch(`${API_BASE_URL}/auth/google`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(googleData)
        });

        if (res.ok) {
            // Backend trả về User ID thật trong DB
            const dbUser = await res.json();

            // Lưu vào localStorage
            localStorage.setItem('currentUser', JSON.stringify(dbUser));

            // Chuyển thẳng vào trang chủ
            alert("Đăng nhập Google thành công! Xin chào " + dbUser.fullName);
            window.location.href = "index.html";
        } else {
            const errText = await res.text();
            alert("Lỗi Google Login: " + errText);
        }

    } catch (error) {
        console.error("Lỗi kết nối:", error);
        alert("Lỗi kết nối Server!");
    }
}

// 4. Hàm giải mã JWT (Copy vào đây để file chạy độc lập)
function decodeJwtResponse(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
}