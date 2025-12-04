package com.project.mange.dto;

import lombok.Data;

@Data
public class SongRequestDTO {
    private String title;
    private String artist;
    private String audioUrl;
    private String coverImage;

    private Long genreId;
    private Long userId;
}
