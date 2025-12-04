package com.project.mange.service;

import com.project.mange.dto.GenreDTO;
import com.project.mange.model.Genre;
import com.project.mange.repository.GenreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {
    @Autowired
    private GenreRepo genreRepo;

    public List<GenreDTO> getAllGenres(){
        List<Genre> genres = genreRepo.findAll();

        return genres.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private GenreDTO convertToDTO(Genre genre){
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setId(genre.getId());
        genreDTO.setName(genre.getName());
        genreDTO.setDescription(genre.getDescription());
        return genreDTO;
    }
}
