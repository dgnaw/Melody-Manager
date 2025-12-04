package com.project.mange.service;

import com.project.mange.model.Song;
import com.project.mange.repository.GenreRepo;
import com.project.mange.repository.SongRepo;
import com.project.mange.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

    @Autowired
    private SongRepo songRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GenreRepo genreRepo;

    public List<Song> getAllSongs() {
        return songRepo.findAll();
    }

}

