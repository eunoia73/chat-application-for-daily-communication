//package com.one.social_project.domain.file.service;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.PutObjectResult;
//import com.one.social_project.domain.file.FileUtil;
//import com.one.social_project.domain.file.dto.ChatFileDTO;
//import com.one.social_project.domain.file.dto.FileDTO;
//import com.one.social_project.domain.file.entity.File;
//import com.one.social_project.domain.file.entity.FileCategory;
//import com.one.social_project.domain.file.error.FileNotFoundException;
//import com.one.social_project.domain.file.repository.FileRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@Transactional
//@ExtendWith(MockitoExtension.class)
//class FileServiceTest {
//    @InjectMocks
//    private FileService fileService;
//    @Mock
//    private FileRepository fileRepository;
//
//    @Value("${cloud.aws.s3.bucket-name}")
//    private String bucketName;
//
//    @Value("${cloud.aws.region.static}")
//    private String region;
//
//    @Mock
//    private AmazonS3 s3Client;  // @MockBean을 사용하여 AmazonS3 모킹
//    @Mock
//    private FileUtil fileUtil;
//
//    private FileDTO fileDTO;
//    private ChatFileDTO chatFileDTO;
//    private File fileEntity;
//
//    @BeforeEach
//    void setUp() {
//        fileDTO = new FileDTO();
//        fileDTO.setFileName("test.png");
//        fileDTO.setFileSize(1024L);
//        fileDTO.setFileType("image/png");
//        fileDTO.setCategory(FileCategory.PROFILE);
//        fileDTO.setNickname("testUser");
//        fileDTO.setFileInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
//
//        chatFileDTO = new ChatFileDTO();
//        chatFileDTO.setFileName("chat.png");
//        chatFileDTO.setFileSize(2048L);
//        chatFileDTO.setFileType("image/png");
//        chatFileDTO.setCategory(FileCategory.CHAT);
//        chatFileDTO.setNickname("testUser");
//        chatFileDTO.setRoomId("room123");
//        chatFileDTO.setFileInputStream(new ByteArrayInputStream(new byte[]{4, 5, 6}));
//
//        fileEntity = File.builder()
//                .fileId("file123")
//                .fileName("test.png")
//                .fileType("image/png")
//                .fileSize(1024L)
//                .originFileUrl("https://s3.amazonaws.com/bucket/test.png")
//                .thumbNailUrl("https://s3.amazonaws.com/bucket-resized/test.png")
//                .expiredAt(LocalDateTime.now().plusDays(7))
//                .category(FileCategory.PROFILE)
//                .nickname("testUser")
//                .build();
//    }
//
//    /**
//     * 1. 파일 업로드 테스트
//     */
//    @Test
//    @WithMockUser(username = "testuser", roles = "USER")  // 인증된 사용자로 모킹
//    void testUploadFile_Success() throws IOException {
//
//        // given
//        Mockito.doNothing().when(fileUtil).validateFile(fileDTO);
//        Mockito.when(fileRepository.save(Mockito.any(File.class))).thenReturn(fileEntity);
//        Mockito.when(s3Client.putObject(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(new PutObjectResult());
//
//        // when
//        FileDTO uploadedFile = fileService.uploadFile(fileDTO);
//
//        // then
//        assertNotNull(uploadedFile);
//        assertEquals("test.png", uploadedFile.getFileName());
//    }
////
////    /**
////     * 파일 업로드 삭제 test
////     *
////     * @throws IOException
////     */
////    @Test
////    @DisplayName("db 파일 업로드 및 삭제 test")
////    void testUploadFileToDatabase() throws IOException {
////        // given
////        FileDTO fileDTO = FileDTO.builder()
////                .fileName("test.jpg")
////                .fileType("image/jpeg")
////                .fileSize(1000L)
////                .fileInputStream(new ByteArrayInputStream(new byte[1000]))
////                .build();
////
////        // when
////        FileDTO savedFileDTO = fileService.uploadFile(fileDTO);
////
////        // then
////        assertNotNull(savedFileDTO);
////
////        //생성되는 UUID 제거하고 파일 이름만 비교
////        String expectedFileName = fileDTO.getFileName();
////        String actualFileName = savedFileDTO.getFileName().substring(savedFileDTO.getFileName().indexOf('_') + 1);
////        assertEquals(expectedFileName, actualFileName);
////
////        //파일 삭제
////        int result = fileService.deleteFile(savedFileDTO.getId());
////        assertEquals(1, result);
////        assertTrue(fileRepository.findById(savedFileDTO.getId()).isEmpty());
////    }
////
////    /**
////     * 파일 조회 기능
////     */
////    @Test
////    @DisplayName("파일 조회_성공")
////    public void testGetFile_FileFound() {
////
////        // 파일이 존재하는 경우, fileRepository의 동작을 모킹
////        when(fileRepository.findById(1L)).thenReturn(Optional.<File>of(file));
////
////        // 서비스 메서드를 호출하고 결과를 검증
////        FileDTO result = fileService.getFile(1L);
////
////        // 반환된 파일이 null이 아니고, id와 파일명이 일치하는지 확인
////        assertNotNull(result);
////        assertEquals(1L, result.getId());
////        assertEquals("test-file.txt", result.getFileName());
////    }
////
////    @Test
////    @DisplayName("파일 조회_실패")
////    public void testGetFile_FileNotFound() {
////        // Mock 동작 정의: ID가 1L인 파일이 없는 경우
////        when(fileRepository.findById(1L)).thenReturn(java.util.Optional.empty());
////
////        //service 호출하면 FileNotFound 에러가 나야 한다
////        assertThrows(FileNotFoundException.class, () -> fileService.getFile(1L));
////    }
////
////    /**
////     * 파일 다운로드 기능
////     */
////    @Test
////    public void testDownloadFile_Success() throws IOException {
////        // Mock 동작 정의: 파일 조회와 S3 객체 반환
////        when(fileRepository.findById(1L)).thenReturn(java.util.Optional.of(file));
////
////        S3Object s3Object = mock(S3Object.class);
////        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(
////                new ByteArrayInputStream("file-content".getBytes()), null));
////        when(s3Client.getObject(bucketName, "test-file.txt")).thenReturn(s3Object);
////
////        // 테스트 실행
////        byte[] result = fileService.downloadFile(1L);
////
////        // 검증
////        assertNotNull(result);
////        assertEquals("file-content", new String(result)); // 변환된 바이트 배열 확인
////    }
////
//
//}