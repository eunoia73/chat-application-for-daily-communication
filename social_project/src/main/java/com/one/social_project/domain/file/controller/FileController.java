package com.one.social_project.domain.file.controller;

import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.entity.File;
import com.one.social_project.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 파일 업로드
     *
     * @param files
     * @return
     * @throws IOException
     */

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("file") List<MultipartFile> files) throws IOException {


        List<ChatFileDTO> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            // 파일이 비어있는지 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }

            // 파일 이름 HTML 태그 방지
            String escapedFileName = escapeHtml(file.getOriginalFilename());

            // 파일 DTO로 변환
            ChatFileDTO chatFileDTO = ChatFileDTO.builder()
                    .fileName(escapedFileName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileInputStream(file.getInputStream())
                    .build();

            // 파일을 서비스로 업로드
            ChatFileDTO savedDTO = fileService.uploadFile(chatFileDTO);

            // 업로드된 파일 정보 추가
            uploadedFiles.add(savedDTO);
        }

        return ResponseEntity.ok(uploadedFiles);
    }

//    // 파일 확장자 추출
//    private String getFileExtension(String fileName) {
//        int dotIndex = fileName.lastIndexOf(".");
//        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1).toLowerCase();
//    }

    //HTML 태그 방지
    private String escapeHtml(String input) {
        return input.replaceAll("[<>&\"']", "");
    }

    /**
     * 파일 삭제
     *
     * @param id
     * @return
     */
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

    /**
     * 파일 조회
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable("id") Long id) {
        ChatFileDTO fileDTO = fileService.getFile(id);
        if (fileDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "파일이 존재하지 않습니다."));
        }
        return ResponseEntity.ok(fileDTO);
    }

    /**
     * 파일 다운로드
     *
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Long id) throws IOException {
        byte[] bytes = fileService.downloadFile(id);
        ChatFileDTO file = fileService.getFile(id);

        //헤더 작성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(file.getFileType()));
        String fileName = URLEncoder.encode(
                file.getFileName(), "UTF-8").replace("+", "%20");
        //"Content-Disposition", "attachment; filename=\"" + fileNm + "\""
        httpHeaders.setContentDispositionFormData("attachment", fileName);
        httpHeaders.setContentLength(file.getFileSize());

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(bytes);

    }
}