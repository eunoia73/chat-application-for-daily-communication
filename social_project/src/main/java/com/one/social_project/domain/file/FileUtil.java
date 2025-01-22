package com.one.social_project.domain.file;

import com.one.social_project.domain.file.dto.FileDTO;
import com.one.social_project.domain.file.entity.FileCategory;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
public class FileUtil {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf", "txt", "doc", "ppt", "gif", "mp4", "zip", "docx", "pptx", "xlsx", "xls");
    private static final List<String> ALLOWED_EXTENSIONS_PROFILE = Arrays.asList("jpg", "jpeg", "png", "gif");

    public void validateFile(FileDTO fileDTO) throws IOException {

        String fileExtension = getFileExtension(fileDTO.getFileName());


        // 파일 확장자 검증
        if (fileDTO.getCategory() == FileCategory.PROFILE) {
            if (!ALLOWED_EXTENSIONS_PROFILE.contains(fileExtension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
            }
        } else if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        // MIME 타입 검증
        validateMimeType(fileDTO);
    }

    // Tika를 이용하여 확장자 임의 변경 파일 검증
    private void validateMimeType(FileDTO fileDTO) throws IOException {

        InputStream originalInputStream = fileDTO.getFileInputStream();

        // InputStream 데이터를 메모리에 복사
        byte[] inputBytes;
        try (originalInputStream) {
            inputBytes = originalInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Tika로 MIME 타입 감지
        Tika tika = new Tika();
        String mimeType = tika.detect(new ByteArrayInputStream(inputBytes));

        // 파일 타입 검증
        if (!fileDTO.getFileType().equalsIgnoreCase(mimeType)) {
            throw new IllegalArgumentException("파일 확장자와 MIME 타입이 일치하지 않습니다.");
        }

        // S3 업로드용 InputStream 복제
        InputStream s3InputStream = new ByteArrayInputStream(inputBytes);

        // 파일 DTO에 새 InputStream 설정
        fileDTO.setFileInputStream(s3InputStream);
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}

