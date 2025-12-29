package com.project.mange.controller;

import com.project.mange.dto.SongRequestDTO;
import com.project.mange.dto.SongResponseDTO;
import com.project.mange.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {
    @Autowired
    private SongService songService;

    @GetMapping
    public ResponseEntity<?>getAllSongs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size){
        if (userId != null) {
            // Nếu có userId -> Trả về nhạc của riêng người đó
            return ResponseEntity.ok(songService.getSongsByUserPaginated(userId, keyword, page, size));
        } else {
            // Nếu không gửi userId -> Trả về tất cả (hoặc danh sách rỗng tùy bạn)
            return ResponseEntity.ok(songService.getAllSongs());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getSongById(@PathVariable Long id) {
        try{
            return ResponseEntity.ok(songService.getSongById(id));
        }catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addSong(@RequestParam("file") MultipartFile file,    // Hứng file từ formData.append("file", ...)
                                     @RequestParam("title") String title,         // Hứng title từ formData.append("title", ...)
                                     @RequestParam("artist") String artist,
                                     @RequestParam("userId") Long userId) {
        try{
            return ResponseEntity.ok(songService.addSong(file, title, artist, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSong(@PathVariable Long id, @RequestBody SongRequestDTO request) {
        try{
            return ResponseEntity.ok(songService.updateSong(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSong(@PathVariable Long id) {
        try{
            songService.deleteSong(id);
            return ResponseEntity.ok("Xóa thành công!");
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
