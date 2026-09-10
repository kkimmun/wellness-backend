package com.kh.wellness.member.model.service;

import java.io.IOException;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.member.model.dao.MemberProfileMapper;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import com.kh.wellness.member.model.dto.PasswordUpdateRequest;
import com.kh.wellness.token.model.dao.TokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {
    private final MemberProfileMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenMapper tokenMapper;
    private final FileService fileService;
    private final S3Service s3Service;

    public MemberProfileResponse getProfile(Long memberNo) {
        MemberProfileResponse profile = mapper.findProfile(memberNo);
        if (profile == null) throw new NotFoundException("회원정보를 찾을 수 없습니다.");
        return profile;
    }

    @Transactional
    public MemberProfileResponse updateName(Long memberNo, String name) {
        if (name == null || !name.matches("^\\S{2,12}$"))
            throw new BadRequestException("닉네임은 공백 없이 2~12자여야 합니다.");
        if (mapper.updateName(memberNo, name) != 1) throw new NotFoundException("회원정보를 찾을 수 없습니다.");
        return getProfile(memberNo);
    }

    @Transactional
    public void updatePassword(Long memberNo, PasswordUpdateRequest request) {
        String oldHash = mapper.findPassword(memberNo);
        if (oldHash == null) throw new BadRequestException("소셜 로그인 계정은 해당 서비스에서 비밀번호를 변경해주세요.");
        if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), oldHash))
            throw new BadRequestException("현재 비밀번호가 일치하지 않습니다.");
        String next = request.getNewPassword();
        if (next == null || !next.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,15}$"))
            throw new BadRequestException("새 비밀번호는 영문과 숫자를 포함한 6~15자여야 합니다.");
        if (passwordEncoder.matches(next, oldHash)) throw new BadRequestException("현재 비밀번호와 다른 비밀번호를 입력해주세요.");
        if (mapper.updatePassword(memberNo, oldHash, passwordEncoder.encode(next)) != 1)
            throw new BadRequestException("비밀번호가 이미 변경되었거나 계정이 비활성화되었습니다. 다시 로그인해주세요.");
        tokenMapper.deleteToken(memberNo);
    }

    @Transactional
    public MemberProfileResponse updatePhoto(Long memberNo, MultipartFile file) {
        getProfile(memberNo);
        validatePhoto(file);
        FileSaveResult result = fileService.store(file, "profile");
        // 요청이 생성한 새 객체만 정리 대상으로 삼는다. 기존 프로필 파일은 삭제하지 않는다.
        String uploadedKey = "profile/" + result.getSaveName();
        boolean cleanupRegistered = false;
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()
                    && TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            cleanupUploadedPhoto(uploadedKey);
                        } else if (status == STATUS_UNKNOWN) {
                            // 커밋 여부가 불확실하면 정상 참조 파일을 삭제하지 않고 운영 확인 대상으로 남긴다.
                            log.error("PROFILE_PHOTO_TRANSACTION_UNKNOWN key={}; DB 참조 여부 확인 필요", uploadedKey);
                        }
                    }
                });
                cleanupRegistered = true;
            }
            if (mapper.updatePhoto(memberNo, file.getOriginalFilename(), result.getSaveName(), result.getImgPath()) != 1)
                throw new NotFoundException("회원정보를 찾을 수 없습니다.");
            return getProfile(memberNo);
        } catch (RuntimeException | Error failure) {
            // 트랜잭션 밖에서 호출되거나 콜백 등록에 실패한 경우에도 보상한다.
            if (!cleanupRegistered) cleanupUploadedPhoto(uploadedKey);
            throw failure;
        }
    }

    private void cleanupUploadedPhoto(String key) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                s3Service.deleteFile(key);
                return;
            } catch (RuntimeException cleanupFailure) {
                if (attempt == 3) {
                    // 원래 DB 오류를 덮어쓰지 않는다. 이 로그의 key로 운영자가 재처리할 수 있다.
                    log.error("PROFILE_PHOTO_CLEANUP_FAILED key={}; 삭제 3회 실패, 재처리 필요", key, cleanupFailure);
                }
            }
        }
    }

    @Transactional
    public MemberProfileResponse removePhoto(Long memberNo) {
        if (mapper.updatePhoto(memberNo, null, null, null) != 1) throw new NotFoundException("회원정보를 찾을 수 없습니다.");
        // 기존 이미지가 다른 화면에서도 사용될 수 있어 S3 객체는 즉시 물리 삭제하지 않는다.
        return getProfile(memberNo);
    }

    private void validatePhoto(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 1024 * 1024)
            throw new BadRequestException("프로필 사진은 1MB 이하의 이미지로 선택해주세요.");
        if (!Set.of("image/jpeg", "image/png", "image/gif", "image/webp").contains(String.valueOf(file.getContentType())))
            throw new BadRequestException("JPG, PNG, GIF, WEBP 이미지 파일만 사용할 수 있습니다.");
        try (var stream = file.getInputStream()) {
            byte[] bytes = stream.readNBytes(12);
            String type = file.getContentType();
            boolean valid = switch (type) {
                case "image/jpeg" -> bytes.length >= 3 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255;
                case "image/png" -> bytes.length >= 8 && java.util.Arrays.equals(java.util.Arrays.copyOf(bytes, 8), new byte[]{(byte)137,80,78,71,13,10,26,10});
                case "image/gif" -> bytes.length >= 6 && (new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).matches("GIF8[79]a"));
                case "image/webp" -> bytes.length >= 12 && new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
                default -> false;
            };
            if (!valid) throw new BadRequestException("이미지 파일 형식을 확인해주세요.");
        } catch (IOException error) {
            throw new BadRequestException("이미지 파일을 읽을 수 없습니다.");
        }
    }
}
