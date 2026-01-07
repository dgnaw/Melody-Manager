package com.project.mange.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String artist;
    @Column(name = "duration")
    private String duration;
    private String audioUrl; // link file nhac
    private String coverImage; // link anh bia

    private LocalDate releaseDate;

    private int views = 0; // Dem luot nghe

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User uploader;


}
