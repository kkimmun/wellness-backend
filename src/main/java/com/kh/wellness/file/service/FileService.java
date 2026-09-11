package com.kh.wellness.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.file.dto.FileSaveResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public FileSaveResult store(MultipartFile file, String subDirectory) {
        if (!isImageFile(file)) {
            throw new BadRequestException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)");
        }

        try {
            String extension = getExtension(file);
            String saveName = UUID.randomUUID().toString() + extension;

            // S3에 저장될 키 (예: plant/uuid.jpg)
            String key = subDirectory + "/" + saveName;

            // S3 업로드
            s3Service.fileSave(file, key);

            // 이미지 경로 생성
            String imgPath = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + subDirectory + "/";

            return new FileSaveResult(saveName, imgPath);
        } catch (S3Exception e) {
            log.error("S3 이미지 저장 실패: statusCode={}, errorCode={}",
                    e.statusCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorCode(),
                    e);
            throw new InternalServerException("이미지 저장소 권한 또는 설정을 확인해주세요.", e);
        } catch (RuntimeException e) {
            log.error("이미지 저장소 처리 실패", e);
            throw new InternalServerException("이미지 저장소에 파일을 저장하지 못했습니다.", e);
        }
    }
	
	// 파일 확장자 추출 (
	private String getExtension(MultipartFile file) {
		String originalName = file.getOriginalFilename();
		if (originalName == null || !originalName.contains(".")) {
			return "";
		}
		return originalName.substring(originalName.lastIndexOf("."));
	}

	// 이미지 파일인지 검증
	private boolean isImageFile(MultipartFile file) {
	    if (file == null || file.isEmpty()) {
	        return false;
	    }

	    // 1) MIME 체크
	    String contentType = file.getContentType();
	    if (contentType == null || !contentType.startsWith("image/")) {
	        return false;
	    }

	    // 2) 확장자 체크
	    String extension = getExtension(file).toLowerCase();
	    boolean extOk = extension.equals(".jpg") || extension.equals(".jpeg")
	            || extension.equals(".png") || extension.equals(".gif")
	            || extension.equals(".webp");
	    if (!extOk) {
	        return false;
	    }

	    // 3) 매직 넘버 검사
	    try (InputStream is = file.getInputStream()) {
	        byte[] head = is.readNBytes(12);
	        if (head.length < 12) {
	            return false;
	        }
	        boolean jpg  = head[0]==(byte)0xFF && head[1]==(byte)0xD8 && head[2]==(byte)0xFF;
	        boolean png  = head[0]==(byte)0x89 && head[1]==0x50 && head[2]==0x4E && head[3]==0x47;
	        boolean gif  = head[0]==0x47 && head[1]==0x49 && head[2]==0x46;
	        boolean webp = head[0]==0x52 && head[1]==0x49 && head[2]==0x46 && head[3]==0x46
	                    && head[8]==0x57 && head[9]==0x45 && head[10]==0x42 && head[11]==0x50;
	        return ((extension.equals(".jpg") || extension.equals(".jpeg")) && jpg)
	                || (extension.equals(".png") && png)
	                || (extension.equals(".gif") && gif)
	                || (extension.equals(".webp") && webp);
	    } catch (IOException e) {
	        return false;
	    }
	}
	
}
