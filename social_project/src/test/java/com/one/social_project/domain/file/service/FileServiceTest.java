package com.one.social_project.domain.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.repository.FileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FileServiceTest {
    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AmazonS3 s3Client;  // @MockBean을 사용하여 AmazonS3 모킹

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Test
    @DisplayName("db 파일 업로드 및 삭제 test")
    void uploadFileToDatabase() throws IOException {
        // given
        ChatFileDTO fileDTO = ChatFileDTO.builder()
                .fileName("test.jpg")
                .fileType("image/jpeg")
                .fileSize(1000L)
                .fileInputStream(new ByteArrayInputStream(new byte[1000]))
                .build();

        // when
        ChatFileDTO savedFileDTO = fileService.uploadFile(fileDTO);

        // then
        assertNotNull(savedFileDTO);

        //생성되는 UUID 제거하고 파일 이름만 비교
        String expectedFileName = fileDTO.getFileName();
        String actualFileName = savedFileDTO.getFileName().substring(savedFileDTO.getFileName().indexOf('_') + 1);
        assertEquals(expectedFileName, actualFileName);

        //파일 삭제
        int result = fileService.deleteFile(savedFileDTO.getId());
        assertEquals(1, result);
        assertTrue(fileRepository.findById(savedFileDTO.getId()).isEmpty());


    }


}