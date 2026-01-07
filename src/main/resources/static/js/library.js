// --- static/js/library.js ---
let currentPage = 0;
const pageSize = 5;
let totalPages = 0;
let isFetching = false; // Chan spam click
let currentKeyword = "";
// Hàm khởi tạo trang Library
async function initLibraryPage() {
   currentPage = 0;
   currentKeyword = "";
   document.getElementById('searchInput').value = "";
   await fetchSongs(currentPage);
   setupSearchEvent();
}

async function fetchSongs(page){
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser || !currentUser.token) {
        console.error("Thiếu Token! Vui lòng đăng nhập lại.");
        return;
    }

    isFetching = true;

    try{
        let url = `${API_BASE_URL}/songs?userId=${currentUser.id}&page=${page}&size=${pageSize}`;
        if (currentKeyword) {
            url += `&keyword=${encodeURIComponent(currentKeyword)}`;
        }
        const response = await fetch(url,{
            method: 'GET',
            headers: {
                'Authorization' : `Bearer ${currentUser.token}`,
                'Content-Type' : 'application/json'
            }
        });
        if (response.ok){
            const data = await response.json();
            const songs = data.content;
            totalPages = data.totalPages;
            currentPage = data.number; // Cập nhật lại cho chắc chắn

            // 1. Cập nhật Playlist hiện tại (Chỉ gồm 5 bài của trang này)
            // Lưu ý: Nếu muốn play hết list thì logic Player phải sửa lại để tự load trang sau.
            // Tạm thời ta chấp nhận Playlist chỉ có 5 bài đang hiện.
            if (typeof currentPlaylist !== 'undefined') {
                currentPlaylist = songs;
            }

            renderSongList(songs);

            renderPaginationButtons();
        }else{
            if (response.status === 401 || response.status === 403) {
                alert("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!");
                localStorage.removeItem('currentUser');
                window.location.href = 'login.html';
            } else {
                console.error("Lỗi server:", response.status);
            }
        }
    }catch (error){
        console.error("Lỗi kết nối:", error)
    }finally {
        isFetching = false;
    }
}

function setupSearchEvent(){
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;
    let timeout = null;

    // Ham tim kiem theo ki tu
    searchInput.addEventListener('input', function (e){
        clearTimeout(timeout);
        const keyword = e.target.value;

        // Loc danh sach tu ban goc
        timeout = setTimeout(() => {
            currentKeyword = keyword;
            currentPage = 0;
            fetchSongs(0);
        }, 500);
    })
}

// Hàm vẽ danh sách bài hát ra HTML
function renderSongList(songs) {
    // Tự động tìm container (div có class .song-list)
    const container = document.querySelector('.song-list');

    // Nếu không tìm thấy container (do chưa load HTML xong) thì dừng
    if (!container) return;

    container.innerHTML = ''; // Xóa nội dung cũ

    // Kiểm tra danh sách trống
    if (!songs || songs.length === 0) {
        container.innerHTML = '<p style="text-align:center; color:#ccc; margin-top:20px;">Thư viện trống. Hãy upload bài hát mới!</p>';
        return;
    }

    // Duyệt qua từng bài hát và tạo HTML
    songs.forEach((song, index) => {
        const realIndex = (currentPage * pageSize) + index + 1;

        // Xử lý ảnh bìa: Nếu backend trả về null thì dùng ảnh mặc định
        // Nếu backend trả về đường dẫn tương đối, cần check để thêm domain
        let imageUrl = 'https://picsum.photos/50/50'; // Mặc định

        if (song.coverImage) { // Nếu backend trả về tên trường là coverImage (hoặc check DTO của bạn)
            // Kiểm tra xem link có phải là http không, nếu không thì cộng localhost vào
            if (song.coverImage.startsWith('http')) {
                imageUrl = song.coverImage;
            } else {
                // BASE_URL lấy từ main.js (http://localhost:8080)
                imageUrl = `${BASE_URL}${song.coverImage}`;
            }
        }

        const durationDisplay = song.duration ? song.duration : '--:--';

        // Tạo HTML cho từng dòng
        const html = `
            <div class="song-item">
                <div class="song-index">${String(realIndex).padStart(2, '0')}</div>
                
                <div class="song-thumb" style="background-image: url('${imageUrl}');"
                 onclick="playSongByIndex(${index})"></div>
                
                <div class="song-info">
                    <h4>${song.title}</h4> 
                    <p>${song.artist}</p>
                </div>
                
                <div class="song-time">${durationDisplay}</div>
                 <div class="song-actions" onclick="playSongByIndex(${index})">
                    <i class="fa-solid fa-play"></i>
                </div>
                <div class="song-options" onclick="toggleSongMenu(event, ${song.id})">
                    <i class="fa-solid fa-ellipsis-vertical"></i>
                    
                    <div id="menu-${song.id}" class="action-menu">
                    <div class="action-item delete" onclick="deleteSong(event,${song.id},'${song.title}')">
                        <i class="fa-solid fa-trash"></i> Delete Song
                    </div>
                    <div class="action-item" onclick="editSong(event, ${song.id})">
                         <i class="fa-solid fa-pen"></i> Edit Info
                    </div>
                </div>
                </div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', html);
    });
}

// 4. HÀM VẼ NÚT PHÂN TRANG (Logic Backend)
function renderPaginationButtons() {
    const wrapper = document.getElementById('pagination');
    if (!wrapper) return;
    wrapper.innerHTML = "";

    if (totalPages <= 1) return; // Nếu chỉ có 1 trang thì ẩn nút đi

    // --- Nút PREV ---
    const prevBtn = createBtn('<i class="fa-solid fa-chevron-left"></i>', () => {
        if (currentPage > 0) fetchSongs(currentPage - 1);
    });
    if (currentPage === 0) prevBtn.disabled = true; // Đang ở trang đầu thì disable
    wrapper.appendChild(prevBtn);

    // --- Các nút số (Hiển thị trang 1, 2, 3...) ---
    // Backend đếm từ 0, nhưng hiển thị cho user phải từ 1
    for (let i = 0; i < totalPages; i++) {
        const btn = createBtn(i + 1, () => fetchSongs(i)); // i + 1 để hiện số đẹp
        if (i === currentPage) btn.classList.add('active');
        wrapper.appendChild(btn);
    }

    // --- Nút NEXT ---
    const nextBtn = createBtn('<i class="fa-solid fa-chevron-right"></i>', () => {
        if (currentPage < totalPages - 1) fetchSongs(currentPage + 1);
    });
    if (currentPage === totalPages - 1) nextBtn.disabled = true; // Đang ở trang cuối thì disable
    wrapper.appendChild(nextBtn);
}

// Helper tạo nút nhanh
function createBtn(html, onClick) {
    const btn = document.createElement('button');
    btn.innerHTML = html;
    btn.classList.add('pagination-btn');
    btn.addEventListener('click', onClick);
    return btn;
}

function toggleSongMenu(event, songId){
    event.stopPropagation(); // Ngan khong cho click lan ra ngoai

    const menu = document.getElementById(`menu-${songId}`);
    document.querySelectorAll('.action-menu.show').forEach(el => {
        if (el.id !== `menu-${songId}`) el.classList.remove('show');
    });

    if (menu) menu.classList.toggle('show');
}

async function deleteSong(event, songId, songTitle){
    event.stopPropagation();
    if (!confirm(`Bạn có chắc muốn xóa bài hát: "${songTitle}" không?`)) return;
    try{
        const response = await fetch(`${API_BASE_URL}/songs/${songId}`, {
            method: 'DELETE'
        })

        if (response.ok){
            if (typeof addNotification === "function") {
                addNotification(`Đã xóa bài hát: ${songTitle}`);
            }
            initLibraryPage();
        }else{
            alert('Lỗi xảy ra khi xóa bài hát!');
        }
    }catch(error){
        console.error('Lỗi xóa: ', error);
    }
}

function editSong(event, songId) {
    event.stopPropagation();
    alert("Chức năng sửa đang phát triển cho ID: " + songId);
    // Logic: Mở modal sửa, điền thông tin cũ, gọi API PUT...
}

window.addEventListener('click', () => {
    document.querySelectorAll('.action-menu.show').forEach(el => el.classList.remove('show'));
});