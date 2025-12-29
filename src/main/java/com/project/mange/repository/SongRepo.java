package com.project.mange.repository;

import com.project.mange.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SongRepo extends JpaRepository<Song, Long> {
   boolean existsByGenreId(Long id);
   Page<Song> findByUploader_Id(Long uploaderId, Pageable pageable);
   @Query("SELECT s FROM Song s WHERE s.uploader.id = :uid AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.artist) LIKE LOWER(CONCAT('%', :keyword, '%')))")
   Page<Song> searchByKeyword(@Param("uid") Long uploaderId, @Param("keyword") String keyword, Pageable pageable);}
