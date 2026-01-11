// --- static/js/upload.js ---

// Đảm bảo biến này khớp với file main.js hoặc login.js
if (!window.API_BASE_URL) {
    window.API_BASE_URL = window.location.origin + '/api';
}

// --- BIẾN TOÀN CỤC ---
let uploadedFiles = [];
// Khai báo biến DOM
let fileInput, dropAreaEmpty, dropAreaFiles, viewEmpty, viewFiles, uploadedFilesRow, btnStartUpload;

// --- 1. HÀM KHỞI TẠO (Được gọi từ main.js) ---
function initUploadPage() {
    console.log("Đang khởi tạo trang Upload...");

    // A. Lấy lại các Element
    fileInput = document.getElementById('fileInput');
    dropAreaEmpty = document.getElementById('dropAreaEmpty');
    dropAreaFiles = document.getElementById('dropAreaFiles');
    viewEmpty = document.getElementById('viewEmpty');
    viewFiles = document.getElementById('viewFiles');
    uploadedFilesRow = document.getElementById('uploadedFilesRow');
    btnStartUpload = document.getElementById('btnStartUpload'); // Nút "Start Upload"

    // B. Reset trạng thái
    uploadedFiles = [];
    renderFiles();

    // C. Gán sự kiện cho nút Upload (QUAN TRỌNG: Code cũ thiếu phần này)
    if (btnStartUpload) {
        btnStartUpload.onclick = submitFiles;
    }

    // D. Gán sự kiện Drag & Drop
    const dropAreas = [dropAreaEmpty, dropAreaFiles].filter(el => el !== null);

    dropAreas.forEach(area => {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(evt => {
            area.addEventListener(evt, preventDefaults, false);
        });

        ['dragenter', 'dragover'].forEach(evt => {
            area.addEventListener(evt, () => area.classList.add('drag-over'));
        });

        ['dragleave', 'drop'].forEach(evt => {
            area.addEventListener(evt, () => area.classList.remove('drag-over'));
        });

        area.addEventListener('drop', (e) => handleFiles(e.dataTransfer.files));
    });

    // E. Gán sự kiện cho nút chọn file truyền thống
    if(fileInput) {
        fileInput.onchange = function() { handleFiles(this.files); };
    }
}

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

// --- 2. LOGIC XỬ LÝ FILE (UI) ---

function handleFiles(files) {
    // Backend hiện tại chỉ hỗ trợ upload từng bài (theo Controller của bạn)
    // Nhưng UI cho phép chọn nhiều, ta cứ lưu vào mảng, lúc upload sẽ xử lý
    uploadedFiles = [...uploadedFiles, ...Array.from(files)];
    renderFiles();
}

function renderFiles() {
    // A. Hiển thị Empty hoặc List
    if (uploadedFiles.length === 0) {
        if(viewEmpty) viewEmpty.style.display = 'flex';
        if(viewFiles) viewFiles.style.display = 'none';
    } else {
        if(viewEmpty) viewEmpty.style.display = 'none';
        if(viewFiles) viewFiles.style.display = 'flex';

        if(btnStartUpload) {
            btnStartUpload.innerText = `Start Upload (${uploadedFiles.length})`;
            btnStartUpload.disabled = false; // Enable nút lại nếu có file
        }

        // B. Vẽ danh sách file
        if(uploadedFilesRow) {
            uploadedFilesRow.innerHTML = '';

            uploadedFiles.forEach((file, index) => {
                const div = document.createElement('div');
                div.className = 'file-card-item';

                let thumb = '<i class="fa-solid fa-file-lines fa-2x" style="color:#888; margin-bottom:10px;"></i>';

                // Tạo thumbnail nếu là ảnh (dù đây là app nhạc, nhưng cứ để logic này cho đẹp)
                if (file.type.startsWith('image/')) {
                    if (!file.previewUrl) file.previewUrl = URL.createObjectURL(file);
                    thumb = `<img src="${file.previewUrl}" class="file-thumb" style="border-radius:5px;">`;
                } else if (file.type.startsWith('audio/')) {
                    thumb = '<i class="fa-solid fa-music fa-2x" style="color:#5865F2; margin-bottom:10px;"></i>';
                }

                div.innerHTML = `
                    <div class="btn-delete-mini" onclick="removeFile(${index})">
                        <i class="fa-solid fa-xmark"></i>
                    </div>
                    ${thumb}
                    <div class="file-name-mini" title="${file.name}">${file.name}</div>
                `;
                uploadedFilesRow.appendChild(div);
            });
        }
    }
}

function removeFile(index) {
    const fileToRemove = uploadedFiles[index];
    if (fileToRemove.previewUrl) {
        URL.revokeObjectURL(fileToRemove.previewUrl);
    }
    uploadedFiles.splice(index, 1);
    renderFiles();
}

// --- 3. SUBMIT FILE (GỌI API BACKEND) ---

async function submitFiles() {
    // 1. Validate: Kiểm tra mảng uploadedFiles (biến toàn cục)
    if (uploadedFiles.length === 0) {
        alert("Vui lòng chọn ít nhất 1 file!");
        return;
    }

    // 2. Check Auth
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser || !currentUser.id) {
        alert("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!");
        window.location.href = 'login.html';
        return;
    }

    // 3. UI Loading
    const oldText = btnStartUpload.innerText;
    btnStartUpload.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Uploading...';
    btnStartUpload.disabled = true;

    // Lấy thông tin nhập từ giao diện (chỉ dùng khi up 1 bài)
    // Lưu ý: Đảm bảo ID 'titleInput' và 'artistInput' khớp với HTML của bạn
    // (Trong code cũ bạn dùng 'songTitle'/'songArtist', hãy check lại ID bên HTML)
    const titleInputEl = document.getElementById('titleInput') || document.getElementById('songTitle');
    const artistInputEl = document.getElementById('artistInput') || document.getElementById('songArtist');

    const inputTitle = titleInputEl ? titleInputEl.value.trim() : "";
    const inputArtist = artistInputEl ? artistInputEl.value.trim() : "";

    let successCount = 0;
    let errorCount = 0;

    // --- 4. VÒNG LẶP GỬI TỪNG FILE ---
    for (let i = 0; i < uploadedFiles.length; i++) {
        const file = uploadedFiles[i];
        const formData = new FormData();

        // LOGIC ĐẶT TÊN THÔNG MINH
        let finalTitle = "";
        let finalArtist = inputArtist || ""; // Artist có thể dùng chung cho cả Album

        if (uploadedFiles.length === 1) {
            // Nếu chỉ up 1 bài: Ưu tiên tên user nhập. Nếu rỗng thì gửi rỗng để Backend tự quét metadata
            finalTitle = inputTitle;
        } else {
            // Nếu up nhiều bài: Bắt buộc lấy tên file (bỏ đuôi .mp3, .wav...)
            // Ví dụ: "Son Tung - Lac Troi.mp3" -> "Son Tung - Lac Troi"
            finalTitle = file.name.replace(/\.[^/.]+$/, "");
        }

        formData.append("file", file);
        formData.append("title", finalTitle);
        formData.append("artist", finalArtist);
        formData.append("userId", currentUser.id);

        try {
            const response = await fetch(`${window.API_BASE_URL}/songs`, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                successCount++;
                console.log(`Đã xong file: ${file.name}`);
            } else {
                errorCount++;
                console.error(`Lỗi file: ${file.name}`);
            }
        } catch (error) {
            errorCount++;
            console.error("Lỗi kết nối:", error);
        }
    }

    // --- 5. TỔNG KẾT ---
    resetBtn(oldText);

    if (successCount > 0) {
        // Gọi thông báo (Notification System)
        if (typeof addNotification === "function") {
            // Nếu up 1 bài thì hiện tên bài, nhiều bài thì hiện số lượng
            if (successCount === 1 && uploadedFiles.length === 1) {
                const songName = inputTitle || uploadedFiles[0].name;
                addNotification(`Upload thành công: "${songName}"`);
            } else {
                addNotification(`Đã upload thành công ${successCount} bài hát.`);
            }
        }

        alert(`Hoàn tất quá trình upload!\n- Thành công: ${successCount}\n- Thất bại: ${errorCount}`);

        // Reset dữ liệu và chuyển trang
        uploadedFiles = [];
        renderFiles(); // Xóa list trên giao diện

        // Chuyển về Library
        if (typeof loadPage === "function") {
            loadPage('library');
        } else {
            window.location.href = "index.html";
        }
    } else {
        alert("Có lỗi xảy ra, không upload được file nào. Vui lòng kiểm tra lại console.");
    }
}
function resetBtn(oldText) {
    if(btnStartUpload) {
        btnStartUpload.innerHTML = oldText;
        btnStartUpload.disabled = false;
    }
}

