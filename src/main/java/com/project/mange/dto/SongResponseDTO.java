package com.project.mange.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SongResponseDTO {
    private Long id;
    private String title;
    private String artist;
    private String duration;
    private String audioUrl;
    private String coverImage;
    private LocalDate releaseDate;
    private int views;

    private String genreName;
    private String uploaderName;
}
