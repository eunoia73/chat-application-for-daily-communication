package com.one.social_project.domain.file.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.one.social_project.domain.chat.entity.ChatParticipants;
import com.one.social_project.domain.chat.repository.ChatParticipantsRepository;
import com.one.social_project.domain.file.FileUtil;
import com.one.social_project.domain.file.dto.ChatFileDTO;
import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.dto.ProfileFileDTO;
import com.one.social_project.domain.file.entity.File;
import com.one.social_project.domain.file.entity.FileCategory;
import com.one.social_project.domain.file.error.FileNotFoundException;
import com.one.social_project.domain.file.repository.FileRepository;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.service.UserService;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.one.social_project.domain.file.FileUtil.ALLOWED_EXTENSIONS_IMAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FileUtil fileUtil;
    private final AmazonS3 s3Client;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket-name-1}")
    private String bucketName;

    @Value("${cloud.aws.s3.bucket-name-2}")
    private String bucketNameResized;

    private String defaultUrl;
    private String resizedUrl;


    @PostConstruct
    public void init() {
        this.defaultUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
        this.resizedUrl = "https://" + bucketNameResized + ".s3." + region + ".amazonaws.com/";

    }

    /**
     * 파일 업로드
     *
     * @param fileDTO
     * @return
     * @throws IOException
     */
    public FileDTO uploadFile(FileDTO fileDTO) throws IOException {

        String fileId = UUID.randomUUID().toString();

        //category가 CHAT이면, 유저가 room에 속한 참여자인지 확인
        if (fileDTO.getCategory() == FileCategory.CHAT) {
            String roomId = ((ChatFileDTO) fileDTO).getRoomId();
            log.info("roomId={}", roomId);
            List<ChatParticipants> participants = chatParticipantsRepository.findByChatRoomRoomId(roomId);

            log.info("/n participants={}", participants);
            String nickname = ((ChatFileDTO) fileDTO).getNickname();
            log.info("nickname={}", nickname);
            boolean isNicknamePresent = participants.stream()
                    .anyMatch(participant -> participant.getUser().getNickname().equals(nickname));


            if (isNicknamePresent) {
                log.info("Nickname {} is present in participants", nickname);
            } else {
                log.info("Nickname {} is NOT present in participants", nickname);
                throw new IllegalArgumentException("파일 업로드 권한이 없습니다.");
            }
        }

        //파일 검증
        fileUtil.validateFile(fileDTO);

        //1. 파일 이름 변경, url 생성
        String fileName = generateFileName(fileId, fileDTO);
        String originFileUrl = defaultUrl + fileName;

        //2. 파일 업로드
        fileDTO.setFileInputStream(fileDTO.getFileInputStream());
        PutObjectResult putObjectResult = uploadS3(fileDTO, fileName);

        //aws s3에 resized 파일이 잘 저장되었는지 확인(이미지 관련 파일만 저장됨)
        String thumbnailFileUrl = null;
        String fileExtension = getFileExtension(fileDTO.getFileName());
        if (ALLOWED_EXTENSIONS_IMAGE.contains(fileExtension)) {
            thumbnailFileUrl = s3Client.getUrl(bucketNameResized, "resized-" + fileName).toString();
        }
        log.info("thumbnailFileUrl={}", thumbnailFileUrl);


        //3. 파일 DTO를 Entity로 변환하여 db에 저장
        File file = File.builder()
                .fileId(fileId)
                .fileName(fileName)
                .fileType(fileDTO.getFileType())
                .fileSize(fileDTO.getFileSize())
                .originFileUrl(originFileUrl)
                .thumbNailUrl(thumbnailFileUrl)
                .expiredAt(convertToLocalDateTime(putObjectResult.getExpirationTime()))
                .category(fileDTO.getCategory())
                .nickname(fileDTO.getNickname())
                .roomId(fileDTO instanceof ChatFileDTO ? ((ChatFileDTO) fileDTO).getRoomId() : null)  // chat일 때만 roomId 설정
                .build();
        File saved = fileRepository.save(file);


        //PROFILE일 경우, user 테이블 profileImg update
        if (fileDTO.getCategory() == FileCategory.PROFILE) {
            Long userId = userRepository.findByNickname(fileDTO.getNickname())
                    .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND))
                    .getId();
            userService.changeProfileImage(userId, thumbnailFileUrl);
        }

        FileDTO savedDTO = null;

        //4. 저장된 Entity를 DTO로 변환
        if (file.getCategory() == FileCategory.PROFILE) {
            savedDTO = ProfileFileDTO.builder()
                    .fileId(saved.getFileId())
                    .fileName(saved.getFileName())
                    .fileType(saved.getFileType())
                    .fileSize(saved.getFileSize())
                    .originFileUrl(saved.getOriginFileUrl())
                    .thumbNailUrl(thumbnailFileUrl)
                    .createdAt(saved.getCreatedAt())
                    .expiredAt(saved.getExpiredAt())
                    .nickname(saved.getNickname())
                    .category(FileCategory.PROFILE)
                    .build();

        } else if (file.getCategory() == FileCategory.CHAT) {
            savedDTO = ChatFileDTO.builder()
                    .fileId(saved.getFileId())
                    .fileName(saved.getFileName())
                    .fileType(saved.getFileType())
                    .fileSize(saved.getFileSize())
                    .originFileUrl(saved.getOriginFileUrl())
                    .thumbNailUrl(thumbnailFileUrl)
                    .createdAt(saved.getCreatedAt())
                    .expiredAt(saved.getExpiredAt())
                    .nickname(saved.getNickname())
                    .category(FileCategory.CHAT)
                    .roomId(((ChatFileDTO) fileDTO).getRoomId())  // roomId ChatFileDTO에서 가져오기
                    .build();

        }

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
    private String generateFileName(String fileId, FileDTO fileDTO) {
        return fileId + "_" + fileDTO.getFileName();
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1).toLowerCase();
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
    public int deleteFile(String fileId) {

        //1. 파일 아이디로 파일 찾기
        File file = fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));

        try {
            // 2-1. S3 원본 파일 삭제
            s3Client.deleteObject(bucketName, file.getFileName());

            // 2-2. resized 파일 삭제
            s3Client.deleteObject(bucketNameResized, "resized-" + file.getFileName());

            // 3. DB 파일 삭제
            fileRepository.delete(file);

            // 파일이 DB에 여전히 존재하거나 S3에서 삭제되지 않았다면 실패 처리
            if (fileRepository.existsByFileId(fileId) || s3Client.doesObjectExist(bucketName, file.getFileName())) {
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

    public FileDTO getFile(String fileId) {
        File file = fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일이 존재하지 않습니다. id=" + fileId));

        FileDTO fileDTO = null;

        if (file.getCategory() == FileCategory.PROFILE) {
            fileDTO = ProfileFileDTO.builder()
                    .fileId(file.getFileId())
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .originFileUrl(file.getOriginFileUrl())
                    .thumbNailUrl(file.getThumbNailUrl())
                    .createdAt(file.getCreatedAt())
                    .expiredAt(file.getExpiredAt())
                    .nickname(file.getNickname())
                    .category(FileCategory.PROFILE)
                    .build();

        } else if (file.getCategory() == FileCategory.CHAT) {
            fileDTO = ChatFileDTO.builder()
                    .fileId(file.getFileId())
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .originFileUrl(file.getOriginFileUrl())
                    .thumbNailUrl(file.getThumbNailUrl())
                    .createdAt(file.getCreatedAt())
                    .expiredAt(file.getExpiredAt())
                    .nickname(file.getNickname())
                    .category(FileCategory.CHAT)
                    .roomId(file.getRoomId())  // roomId ChatFileDTO에서 가져오기
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
    public byte[] downloadFile(String fileId) throws IOException {

        File file = fileRepository.findByFileId(fileId)
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

