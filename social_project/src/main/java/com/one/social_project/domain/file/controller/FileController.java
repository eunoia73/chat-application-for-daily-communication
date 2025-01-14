package com.one.social_project.domain.file.controller;

import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.entity.File;
import com.one.social_project.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    //파일 업로드
    @PostMapping("/upload")
    public List<ChatFileDTO> uploadFiles(@RequestPart("file") List<MultipartFile> files) throws IOException {

        if (files.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        List<ChatFileDTO> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            // 파일 DTO로 변환
            ChatFileDTO chatFileDTO = ChatFileDTO.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileInputStream(file.getInputStream())
                    .build();

            // 파일을 서비스로 업로드
            ChatFileDTO savedDTO = fileService.uploadFile(chatFileDTO);

            // 업로드된 파일 정보 추가
            uploadedFiles.add(savedDTO);
        }

        return uploadedFiles;
    }

    //파일 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable("id") Long id) {
        int result = fileService.deleteFile(id);
        if (result == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "파일이 존재하지 않습니다."));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 삭제에 실패하였습니다."));
        }
        return ResponseEntity.ok(Map.of("message", "File deleted successfully", "id", id));
    }


    // 개발중..
//    //파일 id로 파일 조회
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getFile(@PathVariable("id") Long id) {
//        File file = fileService.getFile(id);
//        if (file == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "파일이 존재하지 않습니다."));
//        }
//        return ResponseEntity.ok(file);
//    }

}
