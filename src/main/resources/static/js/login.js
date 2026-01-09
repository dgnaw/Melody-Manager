// --- static/js/login.js ---

// 1. CẤU HÌNH API
const API_BASE_URL = window.location.origin + "/api";
const GOOGLE_CLIENT_ID = "346688855682-03o2g4bd3dc4c2b2b4sl6s6vakeiukbv.apps.googleusercontent.com";

// 2. KHỞI TẠO KHI TRANG LOAD
window.onload = function () {
    // Khởi tạo nút Google
    initGoogleLoginButton();
};

// ============================================================
// A. XỬ LÝ ĐĂNG NHẬP THƯỜNG (USERNAME / PASSWORD) - GỌI BACKEND
// ============================================================
async function handleLogin(event) {
    event.preventDefault(); // Chặn reload form

    // Lấy dữ liệu từ input (Đảm bảo ID trong HTML khớp với code này)
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        alert("Vui lòng nhập đầy đủ thông tin!");
        return;
    }

    try {
        // Gọi API Login của Spring Boot
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            // Dữ liệu gửi đi phải khớp với UserLoginDTO trong Java
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (response.ok) {
            // Backend trả về UserResponseDTO (chứa thông tin user)
            const userData = await response.json();

            // Lưu thông tin user vào localStorage
            // Lưu ý: Đảm bảo userData có trường avatar, nếu không thì gán ảnh mặc định
            if (!userData.avatar) {
                userData.avatar = "https://i.pravatar.cc/150?img=11"; // Ảnh mặc định
            }

            localStorage.setItem('currentUser', JSON.stringify(userData));

            alert("Đăng nhập thành công!");
            window.location.href = "index.html"; // Chuyển về trang chủ
        } else {
            // Nếu lỗi (401, 400)
            const errorMsg = await response.text();
            alert("Đăng nhập thất bại: " + errorMsg);
        }

    } catch (error) {
        console.error("Lỗi kết nối:", error);
        alert("Không thể kết nối đến Server Backend!");
    }
}

// ============================================================
// B. XỬ LÝ ĐĂNG NHẬP GOOGLE (CLIENT-SIDE)
// ============================================================
function initGoogleLoginButton() {
    if (typeof google === 'undefined') {
        setTimeout(initGoogleLoginButton, 500);
        return;
    }

    try {
        google.accounts.id.initialize({
            client_id: GOOGLE_CLIENT_ID,
            callback: handleGoogleLoginResponse
        });

        const container = document.getElementById("google-login-btn-container");
        if (container) {
            google.accounts.id.renderButton(
                container,
                {
                    theme: "outline",
                    size: "large",
                    width: "370", // Độ rộng khớp với ô input
                    text: "continue_with",
                    shape: "rectangular",
                    logo_alignment: "center"
                }
            );
        }
    } catch (e) {
        console.error("Lỗi khởi tạo Google Button:", e);
    }
}


async function handleGoogleLoginResponse(response) {
    // 1. Giải mã token Google
    const responsePayload = decodeJwtResponse(response.credential);
    console.log("Google Payload:", responsePayload);

    // 2. Chuẩn bị dữ liệu gửi về Backend
    const googleData = {
        email: responsePayload.email,
        fullName: responsePayload.name,
        avatar: responsePayload.picture
    };

    try {
        // 3. GỌI API BACKEND ĐỂ LƯU DB
        const res = await fetch(`${API_BASE_URL}/auth/google`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(googleData)
        });

        if (res.ok) {
            // 4. Backend trả về User đã được lưu trong DB (có ID thật)
            const dbUser = await res.json();

            // 5. Lưu User CHÍNH CHỦ từ DB vào localStorage
            localStorage.setItem('currentUser', JSON.stringify(dbUser));

            alert("Đăng nhập Google thành công!");
            window.location.href = "index.html";
        } else {
            const errText = await res.text();
            alert("Lỗi lưu DB: " + errText);
        }
    } catch (error) {
        console.error("Lỗi kết nối:", error);
        alert("Không kết nối được tới server!");
    }
}

function decodeJwtResponse(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}