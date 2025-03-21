package com.one.social_project.domain.file.controller;

import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.dto.ProfileFileDTO;
import com.one.social_project.domain.file.entity.FileCategory;
import com.one.social_project.domain.file.error.FileNotFoundException;
import com.one.social_project.domain.file.error.UnsupportedFileFormatException;
import com.one.social_project.domain.file.service.FileService;
import com.one.social_project.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFoundException(FileNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "result", false
                ));
    }

    @ExceptionHandler(UnsupportedFileFormatException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedFileFormatException(UnsupportedFileFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "result", false
                ));
    }


    /**
     * 파일 업로드
     *
     * @param files
     * @return
     * @throws IOException
     */

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 단일 파일 크기 제한: 10MB
    private static final long MAX_REQUEST_SIZE = 50 * 1024 * 1024; // 전체 요청 크기 제한: 50MB

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("file") List<MultipartFile> files,
                                         @RequestParam("category") String category,
                                         @AuthenticationPrincipal User user,
                                         @RequestParam(value = "roomId", required = false) String roomId) throws IOException {

        log.info("upload1={}", files);
        String nickname = user.getNickname();  //유저정보 꺼내오기
        long totalRequestSize = 0;

        List<FileDTO> uploadedFiles = new ArrayList<>();

        // 'chat' 카테고리일 때만 roomId 필수 체크
        if ("chat".equalsIgnoreCase(category) && roomId == null) {
            return ResponseEntity.badRequest().body("roomId는 chat 카테고리에서 필수입니다.");
        }

        log.info("upload2={}", category);

        // category를 FileCategory enum으로 변환
        FileCategory fileCategory;
        try {
            fileCategory = FileCategory.valueOf(category.toUpperCase());  // "profile" -> FileCategory.PROFILE
            log.info("category={}", fileCategory);

        } catch (IllegalArgumentException e) {
            log.info("error={}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid category.");
        }

        log.info("upload={}", category);

        for (MultipartFile file : files) {
            // 파일이 비어있는지 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "result", false
//                        "error", "파일이 비어있습니다."
                ));
            }

            // 개별 파일 크기 검증
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of(
                        "result", false
                ));
//                return ResponseEntity.badRequest().body("파일 크기가 10MB를 초과했습니다: " + file.getOriginalFilename());
            }

            // 전체 요청 크기 계산
            totalRequestSize += file.getSize();
            // 전체 요청 크기 검증
            if (totalRequestSize > MAX_REQUEST_SIZE) {
//                return ResponseEntity.badRequest().body("전체 요청 크기가 50MB를 초과했습니다.");
                return ResponseEntity.badRequest().body(Map.of(
                        "result", false
                ));
            }

            // 파일 이름 HTML 태그 방지
            String escapedFileName = escapeHtml(file.getOriginalFilename());

            // 파일 DTO로 변환
            FileDTO fileDTO = null;
            if (fileCategory == FileCategory.PROFILE) {
                fileDTO = ProfileFileDTO.builder()
                        .fileName(escapedFileName)
                        .nickname(nickname)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .fileInputStream(file.getInputStream())
                        .category(FileCategory.PROFILE)
                        .build();

            } else if (fileCategory == FileCategory.CHAT) {
                fileDTO = ChatFileDTO.builder()
                        .fileName(escapedFileName)
                        .nickname(nickname)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .fileInputStream(file.getInputStream())
                        .category(FileCategory.CHAT)
                        .roomId(roomId)  //chat일때만 추가
                        .build();

            }

            // 파일을 서비스로 업로드
            FileDTO savedDTO = fileService.uploadFile(fileDTO);

            // 파일 업로드 권한이 없을 때 처리
            if (savedDTO == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "result", false
//                        "error", "파일 업로드 권한이 없습니다."
                ));
            }

            // 업로드된 파일 정보 추가
            uploadedFiles.add(savedDTO);
        }
        return ResponseEntity.ok(Map.of(
                "result", true,
                "data", uploadedFiles
        ));
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
    public ResponseEntity<?> deleteFile(@PathVariable("id") String id) {
        int result = fileService.deleteFile(id);
        if (result == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", false
//                            "error", "파일이 존재하지 않습니다."
                    ));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", false
//                            "error", "파일 삭제에 실패하였습니다."
                    ));
        }
        return ResponseEntity.ok(Map.of("result", true,
                "id", id));
    }

    /**
     * 파일 조회 - 원본
     *
     * @param fileId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOriginalFile(@PathVariable("id") String fileId) {
        FileDTO fileDTO = fileService.getFile(fileId);
        if (fileDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("result", false
//                            "error", "파일이 존재하지 않습니다."
                    ));
        }
        // 파일이 Profile일 경우
        if (fileDTO instanceof ProfileFileDTO) {
            return ResponseEntity.ok(Map.of(
                    "result", true,
                    "data", fileDTO  // ProfileFileDTO 반환
            ));
        }

        // 파일이 Chat일 경우
        if (fileDTO instanceof ChatFileDTO) {
            // ChatFileDTO에서 chatMessageId가 있는지 확인
            if (((ChatFileDTO) fileDTO).getRoomId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("result", false
//                                "error", "Chat 파일에 필요한 chatMessageId가 없습니다."
                        ));
            }
            return ResponseEntity.ok(Map.of(
                    "result", true,
                    "data", fileDTO
            ));
        }

        return ResponseEntity.ok(Map.of(
                "result", true,
                "data", fileDTO
        ));
    }

//    /**
//     * 파일조회 - 썸네일
//     *
//     * @param fileId
//     * @return
//     */
//    @GetMapping("/{id}/thumbnail")
//    public ResponseEntity<?> getThumbnailFile(@PathVariable("id") String fileId) {
//        FileDTO fileDTO = fileService.getFile(fileId);
//
//        if (fileDTO == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "파일이 존재하지 않습니다."));
//        }
//
//        if (fileDTO.getThumbNailUrl() == null && !ALLOWED_EXTENSIONS_IMAGE.contains(fileDTO.getFileType())) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "thumbnail이 존재하지 않는 파일입니다."));
//        }
//        log.info("fileDTO.getThumbNailUrl={}", fileDTO.getThumbNailUrl());
//        log.info("fileDTO.getFileType={}", fileDTO.getFileType());
//
//        // 파일이 Profile일 경우
//        if (fileDTO instanceof ProfileFileDTO) {
//            return ResponseEntity.ok(fileDTO);  // ProfileFileDTO 반환
//        }
//
//        // 파일이 Chat일 경우
//        if (fileDTO instanceof ChatFileDTO) {
//            // ChatFileDTO에서 roomId가 있는지 확인
//            if (((ChatFileDTO) fileDTO).getRoomId() == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(Map.of("error", "Chat 파일에 필요한 roomId가 없습니다."));
//            }
//            return ResponseEntity.ok(fileDTO);  // ChatFileDTO 반환
//        }
//
//        return ResponseEntity.ok(fileDTO);
//    }

    /**
     * 파일 다운로드
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("id") String fileId) throws IOException {
        byte[] bytes = fileService.downloadFile(fileId);
        FileDTO file = fileService.getFile(fileId);

        if (file == null || bytes == null) {
            // 파일이 존재하지 않거나 데이터가 없을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "result", false
//                            "error", "파일이 존재하지 않습니다."
                    ));
        }

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