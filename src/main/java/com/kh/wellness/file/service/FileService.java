package com.kh.wellness.file.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.file.dto.FileSaveResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        try {
            if (!isImageFile(file)) {
                throw new BadRequestException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)");
            }

            String extension = getExtension(file);
            String saveName = UUID.randomUUID().toString() + extension;

            // S3에 저장될 키 (예: plant/uuid.jpg)
            String key = subDirectory + "/" + saveName;

            // S3 업로드
            s3Service.fileSave(file, key);

            // 이미지 경로 생성
            String imgPath = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + subDirectory + "/";

            return new FileSaveResult(saveName, imgPath);

        } catch (Exception e) {
            log.error("파일 저장 실패", e);
            throw new BadRequestException("이미지 파일 저장에 실패했습니다.");
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
        
       // log.info("파일명 = {}", file.getOriginalFilename());
       // log.info("Content-Type = {}", file.getContentType());
       // log.info("Size = {}", file.getSize());

        String contentType = file.getContentType();
        
        /* MIME 타입 체크
        if (contentType == null || !contentType.startsWith("image/")) {
        	return false;
        } */

        // 확장자 체크
        String extension = getExtension(file).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg")
        		|| extension.equals(".png") || extension.equals(".gif")
        		|| extension.equals(".webp");
    }
	
}
