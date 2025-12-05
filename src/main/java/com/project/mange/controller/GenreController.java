package com.project.mange.controller;

import com.project.mange.dto.GenreDTO;
import com.project.mange.model.User;
import com.project.mange.repository.UserRepo;
import com.project.mange.service.GenreService;
import com.project.mange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@CrossOrigin(origins = "*")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @Autowired
    private UserRepo userRepo;

    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    private boolean isAdmin(Long id) {
        User user = userRepo.findById(id).orElse(null);
        return user != null && "ADMIN".equals(user.getRole());
    }

    @PostMapping
    public ResponseEntity<?> addGenre(@RequestBody GenreDTO request, @RequestParam Long userId) {
        if (!isAdmin(userId)){
            return ResponseEntity.status(403).body("Bạn không có quyền Admin!");
        }
        try{
            return ResponseEntity.ok(genreService.addGenre(request));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable Long id, @RequestBody GenreDTO request) {
        if (!isAdmin(id)){
            return ResponseEntity.status(403).body("Bạn không có quyền Admin!");
        }
        try{
            return ResponseEntity.ok(genreService.updateGenre(id, request));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long id, @RequestParam Long userId) {
        if (!isAdmin(userId)){
            return ResponseEntity.status(403).body("Bạn không có quyền Admin!");
        }
        try{
            genreService.deleteGenre(id);
            return ResponseEntity.ok("Xóa thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
