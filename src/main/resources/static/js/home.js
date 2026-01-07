// Hàm khởi tạo cho trang Home
// (Được gọi từ main.js khi người dùng bấm vào menu Home)

function initHomePage() {
    console.log("Đang khởi tạo trang Home...");

    // --- VIẾT LOGIC RIÊNG CỦA TRANG HOME TẠI ĐÂY ---

    // Ví dụ: Hiệu ứng khi bấm vào đĩa than
    const recordPlayer = document.querySelector('.record-player');
    const needleArm = document.querySelector('.needle-arm');
    const recordIcon = document.querySelector('.record-icon')

    if (recordPlayer && needleArm) {
        recordPlayer.addEventListener('click', function() {
            // Hiệu ứng nhấc kim đơn giản (toggle class)
            // Bạn có thể thêm class .playing vào CSS để kim quay vào đĩa
            this.classList.toggle('playing');

            if (this.classList.contains('playing')) {
                needleArm.style.transform = "rotate(0deg)"; // Kim chạm đĩa
                recordIcon.classList.add('spinning');
            } else {
                needleArm.style.transform = "rotate(-30deg)"; // Kim nhấc ra
                recordIcon.classList.remove('spinning');
            }
        });
    }
}