package com.one.social_project.domain.file.controller;

import com.one.social_project.domain.chat.service.ChatRoomService;
import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.dto.ProfileFileDTO;
import com.one.social_project.domain.file.entity.FileCategory;
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

import static com.one.social_project.domain.file.FileUtil.ALLOWED_EXTENSIONS_IMAGE;


@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final ChatRoomService chatRoomService;

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
                                         @AuthenticationPrincipal User user,
                                         @RequestParam(value = "roomId", required = false) String roomId) throws IOException {

        String nickname = user.getNickname();  //유저정보 꺼내오기

        List<FileDTO> uploadedFiles = new ArrayList<>();

        // 'chat' 카테고리일 때만 roomId 필수 체크
        if ("chat".equalsIgnoreCase(category) && roomId == null) {
            return ResponseEntity.badRequest().body("roomId는 chat 카테고리에서 필수입니다.");
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
     *
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
            if (((ChatFileDTO) fileDTO).getRoomId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chat 파일에 필요한 chatMessageId가 없습니다."));
            }
            return ResponseEntity.ok(fileDTO);  // ChatFileDTO 반환
        }

        return ResponseEntity.ok(fileDTO);
    }

    /**
     * 파일조회 - 썸네일
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<?> getThumbnailFile(@PathVariable("id") Long id) {
        FileDTO fileDTO = fileService.getFile(id);

        if (fileDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "파일이 존재하지 않습니다."));
        }

        if (fileDTO.getThumbNailUrl() == null && !ALLOWED_EXTENSIONS_IMAGE.contains(fileDTO.getFileType())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "thumbnail이 존재하지 않는 파일입니다."));
        }
        log.info("fileDTO.getThumbNailUrl={}",fileDTO.getThumbNailUrl());
        log.info("fileDTO.getFileType={}",fileDTO.getFileType());

        // 파일이 Profile일 경우
        if (fileDTO instanceof ProfileFileDTO) {
            return ResponseEntity.ok(fileDTO);  // ProfileFileDTO 반환
        }

        // 파일이 Chat일 경우
        if (fileDTO instanceof ChatFileDTO) {
            // ChatFileDTO에서 roomId가 있는지 확인
            if (((ChatFileDTO) fileDTO).getRoomId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Chat 파일에 필요한 roomId가 없습니다."));
            }
            return ResponseEntity.ok(fileDTO);  // ChatFileDTO 반환
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