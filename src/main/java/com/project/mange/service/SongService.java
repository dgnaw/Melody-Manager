package com.project.mange.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.project.mange.dto.SongRequestDTO;
import com.project.mange.dto.SongResponseDTO;
import com.project.mange.model.Song;
import com.project.mange.model.User;
import com.project.mange.repository.SongRepo;
import com.project.mange.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SongService {

    @Autowired
    private SongRepo songRepo;

    @Autowired
    private UserRepo userRepo;

    // Đường dẫn thư mục lưu file (Ngang hàng với file pom.xml)
    private final Path uploadLocation = Paths.get("uploads");

    public SongService() {
        // Tạo thư mục uploads nếu chưa có ngay khi khởi tạo Service
        try {
            if (!Files.exists(uploadLocation)) {
                Files.createDirectories(uploadLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ!", e);
        }
    }

    public SongResponseDTO convertToDTO(Song song) {
        SongResponseDTO songDTO = new SongResponseDTO();
        songDTO.setId(song.getId());
        songDTO.setTitle(song.getTitle());
        songDTO.setArtist(song.getArtist());

        // Trả về đường dẫn đầy đủ (hoặc tương đối tùy frontend xử lý)
        // Nếu frontend đã có BASE_URL, trả về /files/... là ổn
        songDTO.setAudioUrl(song.getAudioUrl());
        songDTO.setCoverImage(song.getCoverImage());

        songDTO.setReleaseDate(song.getReleaseDate());
        songDTO.setViews(song.getViews());
        songDTO.setDuration(song.getDuration());

        if (song.getUploader() != null) {
            songDTO.setUploaderName(song.getUploader().getFullName());
        }
        return songDTO;
    }

    public List<SongResponseDTO> getAllSongs() {
        return songRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Page<SongResponseDTO> getSongsByUserPaginated(Long userId,String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Song> songPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Có keyword -> Gọi hàm search
            songPage = songRepo.searchByKeyword(userId, keyword.trim(), pageable);
        } else {
            // Không có keyword -> Lấy danh sách bình thường
            songPage = songRepo.findByUploader_Id(userId, pageable);
        }

        return songPage.map(this::convertToDTO);
    }

    public SongResponseDTO getSongById(Long id) {
        Song song = songRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát"));
        return convertToDTO(song);
    }

    public SongResponseDTO addSong(MultipartFile file, String titleFromRequest, String artistFromRequest, Long uploaderId) throws IOException {
        // 1. Lưu file nhạc MP3
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Dùng UUID để tránh trùng tên tuyệt đối
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("\\s+", "_");
        Path targetPath = uploadLocation.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Khởi tạo đối tượng Song
        Song song = new Song();
        song.setAudioUrl("/files/" + uniqueFileName); // URL để frontend truy cập
        song.setReleaseDate(LocalDate.now());
        song.setViews(0);
        song.setDuration("--:--"); // Mặc định

        if (uploaderId != null) {
            User uploader = userRepo.findById(uploaderId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            song.setUploader(uploader);
        }
        // Logic ưu tiên tên: Request > Metadata > Filename
        String finalTitle = (titleFromRequest != null && !titleFromRequest.isEmpty()) ? titleFromRequest : null;
        String finalArtist = (artistFromRequest != null && !artistFromRequest.isEmpty()) ? artistFromRequest : "Unknown Artist";

        // 3. Đọc Metadata bằng mp3agic
        try {
            Mp3File mp3file = new Mp3File(targetPath.toFile());

            // --- Lấy Thời lượng (Duration) ---
            long lengthInSeconds = mp3file.getLengthInSeconds();
            if (lengthInSeconds > 0) {
                long minutes = lengthInSeconds / 60;
                long seconds = lengthInSeconds % 60;
                // Định dạng 04:05
                song.setDuration(String.format("%02d:%02d", minutes, seconds));
            }

            // --- Lấy ID3v2 Tag (Thông tin chi tiết) ---
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();

                // Nếu user không nhập artist, thử lấy từ file
                if (finalArtist.equals("Unknown Artist") && id3v2Tag.getArtist() != null) {
                    finalArtist = id3v2Tag.getArtist();
                }
                // Nếu user không nhập title, thử lấy từ file
                if (finalTitle == null && id3v2Tag.getTitle() != null) {
                    finalTitle = id3v2Tag.getTitle();
                }

                // --- Trích xuất Ảnh bìa (Cover Art) ---
                byte[] albumImage = id3v2Tag.getAlbumImage();
                if (albumImage != null) {
                    // Tạo tên file ảnh: ID_unique + .jpg
                    String imageFileName = "cover_" + UUID.randomUUID().toString() + ".jpg";
                    Path imagePath = uploadLocation.resolve(imageFileName);

                    Files.write(imagePath, albumImage);

                    song.setCoverImage("/files/" + imageFileName);
                }
            } else if (mp3file.hasId3v1Tag()) {
                // Fallback cho chuẩn cũ ID3v1 (thường không có ảnh, chỉ có text)
                if (finalTitle == null && mp3file.getId3v1Tag().getTitle() != null) {
                    finalTitle = mp3file.getId3v1Tag().getTitle();
                }
                if (finalArtist.equals("Unknown Artist") && mp3file.getId3v1Tag().getArtist() != null) {
                    finalArtist = mp3file.getId3v1Tag().getArtist();
                }
            }

        } catch (Exception e) {
            System.err.println(">> Lỗi đọc Metadata MP3: " + e.getMessage());
            // Không throw exception để quy trình upload vẫn tiếp tục dù lỗi đọc tag
        }

        // Chốt dữ liệu cuối cùng
        song.setTitle(finalTitle != null ? finalTitle : originalFilename.replace(".mp3", ""));
        song.setArtist(finalArtist);

        // Lưu vào DB
        return convertToDTO(songRepo.save(song));
    }

    public SongResponseDTO updateSong(Long id, SongRequestDTO requestDTO){
        Song existingSong = songRepo.findById(id).
                orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát để sửa!"));

        existingSong.setTitle(requestDTO.getTitle());
        existingSong.setArtist(requestDTO.getArtist());

        // Chỉ update nếu request có dữ liệu (tránh ghi đè null)
        if (requestDTO.getAudioUrl() != null && !requestDTO.getAudioUrl().isEmpty()) {
            existingSong.setAudioUrl(requestDTO.getAudioUrl());
        }
        if (requestDTO.getCoverImage() != null && !requestDTO.getCoverImage().isEmpty()) {
            existingSong.setCoverImage(requestDTO.getCoverImage());
        }

        return convertToDTO(songRepo.save(existingSong));
    }

    public void deleteSong(Long id){
        Song song = songRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại!"));

        // Xóa file vật lý (Optional)
        try {
            if (song.getAudioUrl() != null) {
                String audioFileName = song.getAudioUrl().replace("/files/", "");
                Files.deleteIfExists(uploadLocation.resolve(audioFileName));
            }
            if (song.getCoverImage() != null) {
                String imgFileName = song.getCoverImage().replace("/files/", "");
                Files.deleteIfExists(uploadLocation.resolve(imgFileName));
            }
        } catch (IOException e) {
            System.err.println("Không xóa được file vật lý: " + e.getMessage());
        }

        songRepo.delete(song);
    }
}