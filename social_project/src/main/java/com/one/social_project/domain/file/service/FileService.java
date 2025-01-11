package com.one.social_project.domain.file.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.entity.File;
import com.one.social_project.domain.file.error.FileNotFoundException;
import com.one.social_project.domain.file.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    private String defaultUrl;

    @PostConstruct
    public void init() {
        this.defaultUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
    }

    public ChatFileDTO uploadFile(ChatFileDTO chatFileDTO) throws IOException {


        //1. 파일 이름 변경, url 생성
        String fileName = generateFileName(chatFileDTO);
        String fileUrl = defaultUrl + fileName;

        //2. 파일 업로드
        uploadS3(chatFileDTO, fileName);

        //3. 파일 DTO를 Entity로 변환하여 db에 저장
        File file = File.builder()
                .fileName(fileName)
                .fileType(chatFileDTO.getFileType())
                .fileSize(chatFileDTO.getFileSize())
                .fileUrl(fileUrl)
                .build();
        File saved = fileRepository.save(file);


        ChatFileDTO savedDTO = ChatFileDTO.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .fileType(saved.getFileType())
                .fileSize(saved.getFileSize())
                .fileUrl(saved.getFileUrl())
                .build();

        //4. 파일DTO 반환
        return savedDTO;

    }

    //s3에 파일 업로드
    PutObjectResult uploadS3(ChatFileDTO chatFileDTO, String fileName) {
        return s3Client.putObject(bucketName, fileName, chatFileDTO.getFileInputStream(), getObjectMetadata(chatFileDTO));
    }

    //파일 메타데이터 생성
    private ObjectMetadata getObjectMetadata(ChatFileDTO chatFileDTO) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(chatFileDTO.getFileType());
        metadata.setContentLength(chatFileDTO.getFileSize());
        return metadata;
    }

    //파일 이름 생성
    private String generateFileName(ChatFileDTO chatFileDTO) {
        return UUID.randomUUID() + "_" + chatFileDTO.getFileName();
    }


    //파일 삭제
    public int deleteFile(Long fileId) {

        //1. 파일 아이디로 파일 찾기
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));

        try {
            // 2. S3 파일 삭제
            s3Client.deleteObject(bucketName, file.getFileName());
            // 3. DB 파일 삭제
            fileRepository.delete(file);

            // 파일이 DB에 여전히 존재하거나 S3에서 삭제되지 않았다면 실패 처리
            if (fileRepository.existsById(fileId) || s3Client.doesObjectExist(bucketName, file.getFileName())) {
                return 0;  // 삭제 실패
            }

            return 1;  // 삭제 성공

        } catch (AmazonServiceException e) {
            throw new RuntimeException("S3 서비스 오류로 인해 파일 삭제 실패.", e);
        } catch (Exception e) {
            // 삭제 중 예외 발생 시 내부 서버 오류 처리
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }


    // 개발중..
//    //파일 id로 파일 조회
//    public File getFile(Long fileId) {
//        File file = fileRepository.findById(fileId)
//                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));
//
//        ChatFileDTO chatFileDTO = ChatFileDTO.builder()
//                .id(file.getId())
//                .fileName(file.getFileName())
//                .fileType(file.getFileType())
//                .fileSize(file.getFileSize())
//                .fileUrl(file.getFileUrl())
//                .build();
//        PutObjectResult putObjectResult = getPutObjectResult(chatFileDTO);
//        System.out.println("만료시간" + putObjectResult.getExpirationTime());
//
//
//        System.out.println(file.getFileUrl());
//        return file;
//
//
//    }
//
//    //파일 url로 파일 조회
//    PutObjectResult getPutObjectResult(ChatFileDTO chatFileDTO) {
//        // URL-safe 파일명으로 인코딩
//        String encodedFileName = URLEncoder.encode(chatFileDTO.getFileName(), StandardCharsets.UTF_8);
//        //aws s3에 저장된 파일 url을 통해 파일 조회
//        PutObjectResult putObjectResult = s3Client.putObject(bucketName, encodedFileName, chatFileDTO.getFileUrl());
//        return putObjectResult;
//    }
//

}

