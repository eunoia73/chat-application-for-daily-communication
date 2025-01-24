package com.one.social_project.domain.file.controller;

import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest  // 전체 Spring Context를 로드하는 방법
@AutoConfigureMockMvc  // MockMvc 설정
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;  // MockMvc를 사용하여 HTTP 요청을 테스트할 수 있게 해줌

    @MockitoBean
    private FileService fileService;  // FileService를 Mock 객체로 생성하여 의존성 주입

    /**
     * 파일 업로드 테스트
     *
     * @throws Exception
     */
    @Test
    @DisplayName("파일 업로드_실패_1.파일이 비어있는 경우")
    public void testUploadFiles_emptyFileList() throws Exception {
        // 비어 있는 파일 목록을 업로드 시도
        mockMvc.perform(multipart("/api/files/upload")  // multipart 요청으로 업로드
                        .file("file", new byte[0]))  // 빈 바이트 배열을 포함한 파일 전송
                .andExpect(status().isBadRequest())  // 400 Bad Request 상태 코드 확인
                .andExpect(content().string("파일이 비어있습니다."));
    }

//    @Test
//    @DisplayName("파일 업로드_실패_2.지원하지 않는 파일 형식")
//    public void testUploadFiles_invalidFileExtension() throws Exception {
//        // 지원되지 않는 파일 형식 업로드
//        MultipartFile invalidFile = new MockMultipartFile("file", "test.exe", "application/octet-stream", new byte[1]);
//
//        mockMvc.perform(multipart("/api/files/upload")
//                        .file((MockMultipartFile) invalidFile))  // 잘못된 파일 형식
//                .andExpect(status().isBadRequest())  // 400 Bad Request 상태 코드 확인
//                .andExpect(content().string("지원하지 않는 파일 형식입니다. : exe"));  // "지원하지 않는 파일 형식" 메시지 확인
//    }

    @Test
    @DisplayName("파일 업로드_성공_파일 업로드_HTML 특수문자 처리")
    public void testUploadFiles_htmlEscapedFileName() throws Exception {
        // HTML 태그가 포함된 파일 이름을 업로드하여 특수문자 처리 확인
        MultipartFile invalidFile = new MockMultipartFile("file", "<script>alert('test');</script>.jpg", "image/jpeg", new byte[1]);

        when(fileService.uploadFile(any(FileDTO.class))).thenReturn(
                FileDTO.builder()
                        .fileName("&lt;script&gt;alert('test');&lt;/script&gt;.jpg")
                        .fileType("image/jpeg")
                        .fileSize(1L)
                        .build()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file((MockMultipartFile) invalidFile))  // HTML 특수문자가 포함된 파일 업로드 요청
                .andExpect(status().isOk())  // 200 OK 상태 코드 확인
                .andExpect(content().json("[{\"fileName\":\"&lt;script&gt;alert('test');&lt;/script&gt;.jpg\",\"fileType\":\"image/jpeg\",\"fileSize\":1}]"));  // HTML 태그가 안전하게 처리된 파일 이름 확인
    }

    @Test
    @DisplayName("파일 업로드_성공_유효한 파일")
    public void testUploadFiles_validFiles() throws Exception {
        // 유효한 파일 업로드
        MultipartFile validFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[10]);

        when(fileService.uploadFile(any(FileDTO.class))).thenReturn(
                FileDTO.builder()
                        .fileName("test.jpg")
                        .fileType("image/jpeg")
                        .fileSize(10L)
                        .build()
        );

    }

    @Test
    @DisplayName("여러 파일 업로드_성공")
    public void testUploadMultipleFiles() throws Exception {
        // 여러 파일 업로드
        MultipartFile file1 = new MockMultipartFile("file", "file1.jpg", "image/jpeg", new byte[10]);
        MultipartFile file2 = new MockMultipartFile("file", "file2.png", "image/png", new byte[15]);

        when(fileService.uploadFile(any(FileDTO.class)))
                .thenReturn(FileDTO.builder().fileName("file1.jpg").fileType("image/jpeg").fileSize(10L).build())
                .thenReturn(FileDTO.builder().fileName("file2.png").fileType("image/png").fileSize(15L).build());

        mockMvc.perform(multipart("/api/files/upload")
                        .file((MockMultipartFile) file1)
                        .file((MockMultipartFile) file2))  // 두 파일을 동시에 업로드 요청
                .andExpect(status().isOk())  // 200 OK 상태 코드 확인
                .andExpect(content().json("[{\"fileName\":\"file1.jpg\",\"fileType\":\"image/jpeg\",\"fileSize\":10}," +
                        "{\"fileName\":\"file2.png\",\"fileType\":\"image/png\",\"fileSize\":15}]"));  // 업로드된 파일들의 정보가 배열 형식으로 확인
    }

    /**
     * 파일 삭제 테스트 - 수정 필요
     */
//    @Test
//    @DisplayName("파일 삭제 실패_파일이 존재하지 않는 경우")
//    public void testDeleteFile_notFound() throws Exception {
//        String fileId = 1L;
//
//        // fileService.deleteFile이 0을 반환하도록 설정
//        when(fileService.deleteFile(fileId)).thenReturn(0);
//
//        mockMvc.perform(delete("/api/files/{id}", fileId))
//                .andExpect(status().isNotFound())
//                .andExpect(content().json("{\"error\":\"파일이 존재하지 않습니다.\"}"));
//    }

//    @Test
//    @DisplayName("파일 삭제 실패_파일 삭제 중 오류 발생")
//    public void testDeleteFile_internalServerError() throws Exception {
//        Long fileId = 2L;
//
//        // fileService.deleteFile이 -1을 반환하도록 설정
//        when(fileService.deleteFile(fileId)).thenReturn(-1);
//
//        mockMvc.perform(delete("/api/files/{id}", fileId))
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().json("{\"error\":\"파일 삭제에 실패하였습니다.\"}"));
//    }
//
//    @Test
//    @DisplayName("파일 삭제 성공")
//    public void testDeleteFile_success() throws Exception {
//        Long fileId = 3L;
//
//        // fileService.deleteFile이 1을 반환하도록 설정
//        when(fileService.deleteFile(fileId)).thenReturn(1);
//
//        mockMvc.perform(delete("/api/files/{id}", fileId))
//                .andExpect(status().isOk())
//                .andExpect(content().json("{\"message\":\"File deleted successfully\",\"id\":3}"));
//    }

    /**
     * 파일 조회 테스트 - 수정 필요
     */

//    @Test
//    @DisplayName("파일 조회 실패_파일이 존재하지 않는 경우")
//    public void testGetFile_notFound() throws Exception {
//        Long fileId = 1L;
//
//        // fileService.getFile이 null을 반환하도록 설정
//        when(fileService.getFile(fileId)).thenReturn(null);
//
//        mockMvc.perform(get("/api/files/{id}", fileId))
//                .andExpect(status().isNotFound())
//                .andExpect(content().json("{\"error\":\"파일이 존재하지 않습니다.\"}"));
//    }

//    @Test
//    @DisplayName("파일 조회 성공")
//    public void testGetFile_success() throws Exception {
//        Long fileId = 2L;
//        FileDTO fileDTO = FileDTO.builder()
//                .fileName("test.jpg")
//                .fileType("image/jpeg")
//                .fileSize(100L)
//                .build();
//
//        // fileService.getFile이 fileDTO를 반환하도록 설정
//        when(fileService.getFile(fileId)).thenReturn(fileDTO);
//
//        mockMvc.perform(get("/api/files/{id}", fileId))
//                .andExpect(status().isOk())
//                .andExpect(content().json("{\"fileName\":\"test.jpg\",\"fileType\":\"image/jpeg\",\"fileSize\":100}"));
//    }

}
