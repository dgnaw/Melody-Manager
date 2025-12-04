package com.project.mange.service;

import com.project.mange.dto.SongRequestDTO;
import com.project.mange.dto.SongResponseDTO;
import com.project.mange.model.Genre;
import com.project.mange.model.Song;
import com.project.mange.model.User;
import com.project.mange.repository.GenreRepo;
import com.project.mange.repository.SongRepo;
import com.project.mange.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    @Autowired
    private SongRepo songRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GenreRepo genreRepo;

    public SongResponseDTO convertToDTO(Song song) {
        SongResponseDTO songDTO = new SongResponseDTO();
        songDTO.setId(song.getId());
        songDTO.setTitle(song.getTitle());
        songDTO.setArtist(song.getArtist());
        songDTO.setAudioUrl(song.getAudioUrl());
        songDTO.setCoverImage(song.getCoverImage());
        songDTO.setReleaseDate(song.getReleaseDate());
        songDTO.setViews(song.getViews());

        if (song.getGenre() != null) {
            songDTO.setGenreName(song.getGenre().getName());
        }
        if (song.getUploader() != null) {
            songDTO.setUploaderName(song.getUploader().getFullName());
        }
        return songDTO;
    }

    public List<SongResponseDTO> getAllSongs() {
        List<Song> songs = songRepo.findAll();

        return songs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public SongResponseDTO addSong(SongRequestDTO requestDTO){
        Genre genre = genreRepo.findById(requestDTO.getGenreId()).
                orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại ID: " + requestDTO.getGenreId()));
        User uploader = userRepo.findById(requestDTO.getUserId()).
                orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: "  + requestDTO.getUserId()));

        Song song = new Song();
        song.setTitle(requestDTO.getTitle());
        song.setArtist(requestDTO.getArtist());
        song.setAudioUrl(requestDTO.getAudioUrl());
        song.setCoverImage(requestDTO.getCoverImage());
        song.setReleaseDate(LocalDate.now());
        song.setViews(0);

        song.setGenre(genre);
        song.setUploader(uploader);

        Song saveSong = songRepo.save(song);
        return convertToDTO(saveSong);

    }

}

