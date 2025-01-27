package com.one.social_project.domain.file.repository;

import com.one.social_project.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFileId(String fileId);
    boolean existsByFileId(String fileId);
}
