package com.one.social_project.domain.file.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.one.social_project.domain.file.FileValidator;
import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.dto.ProfileFileDTO;
import com.one.social_project.domain.file.entity.File;
import com.one.social_project.domain.file.entity.FileCategory;
import com.one.social_project.domain.file.error.FileNotFoundException;
import com.one.social_project.domain.file.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FileValidator fileValidator;
    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket-name-1}")
    private String bucketName;

    @Value("${cloud.aws.s3.bucket-name-2}")
    private String bucketNameResized;

    @Value("${cloud.aws.region.static}")
    private String region;

    private String defaultUrl;


    @PostConstruct
    public void init() {
        this.defaultUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
    }

    /**
     * 파일 업로드
     *
     * @param fileDTO
     * @return
     * @throws IOException
     */
    public FileDTO uploadFile(FileDTO fileDTO) throws IOException {

        //파일 검증
        fileValidator.validateFile(fileDTO);

        //1. 파일 이름 변경, url 생성
        String fileName = generateFileName(fileDTO);
        String fileUrl = defaultUrl + fileName;

        //2. 파일 업로드
        //InputStream fileInputStream = fileDTO.getFileInputStream();
        fileDTO.setFileInputStream(fileDTO.getFileInputStream());
        PutObjectResult putObjectResult = uploadS3(fileDTO, fileName);

        //3. 파일 DTO를 Entity로 변환하여 db에 저장
        File file = File.builder()
                .fileName(fileName)
                .fileType(fileDTO.getFileType())
                .fileSize(fileDTO.getFileSize())
                .fileUrl(fileUrl)
                .expiredAt(convertToLocalDateTime(putObjectResult.getExpirationTime()))
                .category(fileDTO.getCategory())
                .chatMessageId(fileDTO instanceof ChatFileDTO ? ((ChatFileDTO) fileDTO).getChatMessageId() : null)  // chat일 때만 chatMessageId 설정
                .build();
        File saved = fileRepository.save(file);

        FileDTO savedDTO = null;
//
//        FileDTO savedDTO = FileDTO.builder()
//                .id(saved.getId())
//                .fileName(saved.getFileName())
//                .fileType(saved.getFileType())
//                .fileSize(saved.getFileSize())
//                .fileUrl(saved.getFileUrl())
//                .createdAt(saved.getCreatedAt())
//                .expiredAt(saved.getExpiredAt())
//                .build();

        if (file.getCategory() == FileCategory.PROFILE) {
            savedDTO = ProfileFileDTO.builder()
                    .id(saved.getId())
                    .fileName(saved.getFileName())
                    .fileType(saved.getFileType())
                    .fileSize(saved.getFileSize())
                    .fileUrl(saved.getFileUrl())
                    .createdAt(saved.getCreatedAt())
                    .expiredAt(saved.getExpiredAt())
                    .category(FileCategory.PROFILE)
                    .build();

        } else if (file.getCategory() == FileCategory.CHAT) {
            savedDTO = ChatFileDTO.builder()
                    .id(saved.getId())
                    .fileName(saved.getFileName())
                    .fileType(saved.getFileType())
                    .fileSize(saved.getFileSize())
                    .fileUrl(saved.getFileUrl())
                    .createdAt(saved.getCreatedAt())
                    .expiredAt(saved.getExpiredAt())
                    .category(FileCategory.CHAT)
                    .chatMessageId(((ChatFileDTO) fileDTO).getChatMessageId())  // chatMessageId는 ChatFileDTO에서 가져오기
                    .build();

        }

        //4. 파일DTO 반환
        return savedDTO;

    }


    //s3에 파일 업로드
    PutObjectResult uploadS3(FileDTO fileDTO, String fileName) {
        return s3Client.putObject(bucketName, fileName, fileDTO.getFileInputStream(), getObjectMetadata(fileDTO));
    }

    //파일 메타데이터 생성
    private ObjectMetadata getObjectMetadata(FileDTO fileDTO) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileDTO.getFileType());
        metadata.setContentLength(fileDTO.getFileSize());
        return metadata;
    }

    //파일 이름 생성
    private String generateFileName(FileDTO fileDTO) {
        return UUID.randomUUID() + "_" + fileDTO.getFileName();
    }

    //Date -> LocalDateTime 변환
    public LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date가 null입니다.");
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 파일 삭제
     *
     * @param fileId
     * @return
     */
    public int deleteFile(Long fileId) {

        //1. 파일 아이디로 파일 찾기
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));

        try {
            // 2-1. S3 원본 파일 삭제
            s3Client.deleteObject(bucketName, file.getFileName());

            // 2-2. resized 파일 삭제
            s3Client.deleteObject(bucketNameResized, "resized-" + file.getFileName());

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

    /**
     * 파일 조회
     *
     * @param fileId
     * @return
     */

    public FileDTO getFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));

        // System.out.println("file!!!!" + file.getCategory());
        //System.out.println(file.getChatMessageId());
//        FileDTO fileDTO = FileDTO.builder()
//                .id(file.getId())
//                .fileName(file.getFileName())
//                .fileType(file.getFileType())
//                .fileSize(file.getFileSize())
//                .fileUrl(file.getFileUrl())
//                .createdAt(file.getCreatedAt())
//                .expiredAt(file.getExpiredAt())
//                .category(file.getCategory())
//                .build();

        FileDTO fileDTO = null;

        if (file.getCategory() == FileCategory.PROFILE) {
            fileDTO = ProfileFileDTO.builder()
                    .id(file.getId())
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .fileUrl(file.getFileUrl())
                    .createdAt(file.getCreatedAt())
                    .expiredAt(file.getExpiredAt())
                    .category(FileCategory.PROFILE)
                    .build();

        } else if (file.getCategory() == FileCategory.CHAT) {
            fileDTO = ChatFileDTO.builder()
                    .id(file.getId())
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .fileUrl(file.getFileUrl())
                    .createdAt(file.getCreatedAt())
                    .expiredAt(file.getExpiredAt())
                    .category(FileCategory.CHAT)
                    .chatMessageId(file.getChatMessageId())  // chatMessageId는 ChatFileDTO에서 가져오기
                    .build();
        }
        return fileDTO;

    }

    /**
     * 파일 다운로드
     *
     * @param fileId
     * @return
     * @throws IOException
     */
    public byte[] downloadFile(Long fileId) throws IOException {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));

        //만약 파일 만료 기간이 지났다면?
        if (file.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new FileNotFoundException("파일이 만료되었습니다. id=" + fileId);
        }

        S3Object s3object = s3Client.getObject(bucketName, file.getFileName());
        if (s3object == null) {
            throw new java.io.FileNotFoundException();
        }
        byte[] bytes = IOUtils.toByteArray(s3object.getObjectContent());// S3 객체를 바이트 배열로 변환


        return bytes;
    }


}

