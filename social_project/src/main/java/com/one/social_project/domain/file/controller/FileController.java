package com.one.social_project.domain.file.controller;

import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.dto.ProfileFileDTO;
import com.one.social_project.domain.file.entity.FileCategory;
import com.one.social_project.domain.file.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket-name-2}")
    private String bucketNameResized;

    private String resizedUrl;
    @PostConstruct
    public void init() {
        this.resizedUrl = "https://" + bucketNameResized + ".s3." + region + ".amazonaws.com/";
    }

    /**
     * 파일 업로드
     *
     * @param files
     * @return
     * @throws IOException
     */

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("file") List<MultipartFile> files,
                                         @RequestParam("category") String category,
//                                         @RequestParam("userId") Long id,
                                         @RequestParam(value = "messageId", required = false) Long messageId) throws IOException {

        List<FileDTO> uploadedFiles = new ArrayList<>();

        // 'chat' 카테고리일 때만 messageId 필수 체크
        if ("chat".equalsIgnoreCase(category) && messageId == null) {
            return ResponseEntity.badRequest().body("messageId는 chat 카테고리에서 필수입니다.");
        }

        // category를 FileCategory enum으로 변환
        FileCategory fileCategory;
        try {
            fileCategory = FileCategory.valueOf(category.toUpperCase());  // "profile" -> FileCategory.PROFILE
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid category.");
        }

        for (MultipartFile file : files) {
            // 파일이 비어있는지 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }

            // 파일 이름 HTML 태그 방지
            String escapedFileName = escapeHtml(file.getOriginalFilename());

            // 파일 DTO로 변환
            FileDTO fileDTO = null;
            if (fileCategory == FileCategory.PROFILE) {
                fileDTO = ProfileFileDTO.builder()
                        .fileName(escapedFileName)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .fileInputStream(file.getInputStream())
                        .category(FileCategory.PROFILE)
                        .build();

            } else if (fileCategory == FileCategory.CHAT) {
                fileDTO = ChatFileDTO.builder()
                        .fileName(escapedFileName)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .fileInputStream(file.getInputStream())
                        .category(FileCategory.CHAT)
                        .chatMessageId(messageId)  //chat일때만 추가
                        .build();

            }

            // 파일을 서비스로 업로드
            FileDTO savedDTO = fileService.uploadFile(fileDTO);

            // 업로드된 파일 정보 추가
            uploadedFiles.add(savedDTO);
        }

        return ResponseEntity.ok(uploadedFiles);
    }


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
     * 파일 조회 - 원본
     * @param id
     * @return
     */
    @GetMapping("/{id}/original")
    public ResponseEntity<?> getOriginalFile(@PathVariable("id") Long id) {
        FileDTO fileDTO = fileService.getFile(id);
        if (fileDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "파일이 존재하지 않습니다."));
        }
        // 파일이 Profile일 경우
        if (fileDTO instanceof ProfileFileDTO) {
            return ResponseEntity.ok(fileDTO);  // ProfileFileDTO 반환
        }

        // 파일이 Chat일 경우
        if (fileDTO instanceof ChatFileDTO) {
            // ChatFileDTO에서 chatMessageId가 있는지 확인
            if (((ChatFileDTO) fileDTO).getChatMessageId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chat 파일에 필요한 chatMessageId가 없습니다."));
            }
            return ResponseEntity.ok(fileDTO);  // ChatFileDTO 반환
        }

        return ResponseEntity.ok(fileDTO);
    }

    /**
     * 파일조회 - 썸네일
     * @param id
     * @return
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<?> getThumbnailFile(@PathVariable("id") Long id) {
        FileDTO fileDTO = fileService.getFile(id);
        System.out.println(fileDTO.getFileUrl());
        //파일 썸네일 url로 변경해주기
        fileDTO.setFileUrl(updateUrl(fileDTO.getFileName()));
        System.out.println(fileDTO.getFileUrl());
        if (fileDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "파일이 존재하지 않습니다."));
        }
        // 파일이 Profile일 경우
        if (fileDTO instanceof ProfileFileDTO) {
            return ResponseEntity.ok(fileDTO);  // ProfileFileDTO 반환
        }

        // 파일이 Chat일 경우
        if (fileDTO instanceof ChatFileDTO) {
            // ChatFileDTO에서 chatMessageId가 있는지 확인
            if (((ChatFileDTO) fileDTO).getChatMessageId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chat 파일에 필요한 chatMessageId가 없습니다."));
            }
            return ResponseEntity.ok(fileDTO);  // ChatFileDTO 반환
        }

        return ResponseEntity.ok(fileDTO);
    }

    //썸네일 관리 버킷 url 전송
    public String updateUrl(String fileName) {
        return resizedUrl + "resized-" + fileName;
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
        FileDTO file = fileService.getFile(id);

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