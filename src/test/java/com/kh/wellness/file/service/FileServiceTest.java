package com.kh.wellness.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.file.dto.FileSaveResult;

import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(fileService, "region", "ap-northeast-2");
    }

    @Test
    void imageIsStoredInRequestedS3Directory() {
        MockMultipartFile image = new MockMultipartFile(
                "imageFiles", "place.png", "image/png", new byte[] { 1, 2, 3 });

        FileSaveResult result = fileService.store(image, "places");

        assertThat(result.getSaveName()).endsWith(".png");
        assertThat(result.getImgPath())
                .isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/places/");
        verify(s3Service).fileSave(image, "places/" + result.getSaveName());
    }

    @Test
    void nonImageFileIsRejectedAsBadRequest() {
        MockMultipartFile text = new MockMultipartFile(
                "imageFiles", "memo.txt", "text/plain", "text".getBytes());

        assertThatThrownBy(() -> fileService.store(text, "places"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미지 파일만 업로드");

        verify(s3Service, never()).fileSave(eq(text), anyString());
    }

    @Test
    void s3PermissionFailureIsReportedAsServerConfigurationError() {
        MockMultipartFile image = new MockMultipartFile(
                "imageFiles", "place.jpg", "image/jpeg", new byte[] { 1, 2, 3 });
        S3Exception forbidden = (S3Exception) S3Exception.builder()
                .statusCode(403)
                .message("Forbidden")
                .build();
        doThrow(forbidden).when(s3Service).fileSave(eq(image), anyString());

        assertThatThrownBy(() -> fileService.store(image, "places"))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("이미지 저장소 권한 또는 설정을 확인해주세요.")
                .hasCause(forbidden);
    }
}
