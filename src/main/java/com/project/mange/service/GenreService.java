package com.project.mange.service;

import com.project.mange.dto.GenreDTO;
import com.project.mange.model.Genre;
import com.project.mange.repository.GenreRepo;
import com.project.mange.repository.SongRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {
    @Autowired
    private GenreRepo genreRepo;

    @Autowired
    private SongRepo songRepo;

    public List<GenreDTO> getAllGenres(){
        List<Genre> genres = genreRepo.findAll();

        return genres.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public GenreDTO addGenre(GenreDTO request){
        if(genreRepo.findByName(request.getName()) != null){
            throw new RuntimeException("Thể loại này đã tồn tại!");
        }
        Genre genre = new Genre();
        genre.setName(request.getName());
        genre.setDescription(request.getDescription());
        return convertToDTO(genreRepo.save(genre));
    }

    public GenreDTO updateGenre(Long id, GenreDTO request){
        Genre genre = genreRepo.findById(id).
                orElseThrow(() -> new RuntimeException("Thể loại này không tồn tại!"));
        genre.setName(request.getName());
        genre.setDescription(request.getDescription());
        return convertToDTO(genreRepo.save(genre));
    }

    public void deleteGenre(Long id){
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Thể loại này không tồn tại!"));
        if (songRepo.existsByGenreId(id)){
            throw new RuntimeException("Không thể xóa! Có bài hát đang thuộc th loại này!");
        }
        genreRepo.delete(genre);
    }


    private GenreDTO convertToDTO(Genre genre){
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setId(genre.getId());
        genreDTO.setName(genre.getName());
        genreDTO.setDescription(genre.getDescription());
        return genreDTO;
    }
}
