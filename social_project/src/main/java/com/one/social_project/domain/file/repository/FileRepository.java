package com.one.social_project.domain.file.repository;

import com.one.social_project.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

}
