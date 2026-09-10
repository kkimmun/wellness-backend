package com.kh.wellness.member.model.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.member.model.dao.MemberProfileMapper;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import com.kh.wellness.member.model.dto.PasswordUpdateRequest;
import com.kh.wellness.token.model.dao.TokenMapper;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {
    @Mock MemberProfileMapper mapper;
    @Mock TokenMapper tokens;
    @Mock FileService files;
    @Mock S3Service storage;
    final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    MemberProfileService service;
    @BeforeEach void setup() { service = new MemberProfileService(mapper, encoder, tokens, files, storage); }
    PasswordUpdateRequest request(String old, String next) {
        var result = new PasswordUpdateRequest(); result.setCurrentPassword(old); result.setNewPassword(next); return result;
    }
    @Test void nicknameDoesNotChangeCredentials() {
        var profile = new MemberProfileResponse(); profile.setMemberName("새닉네임");
        when(mapper.updateName(7L, "새닉네임")).thenReturn(1); when(mapper.findProfile(7L)).thenReturn(profile);
        assertThat(service.updateName(7L, "새닉네임").getMemberName()).isEqualTo("새닉네임");
        verify(mapper, never()).updatePassword(any(), any(), any()); verifyNoInteractions(tokens);
    }
    @Test void invalidNicknameDoesNotWrite() {
        assertThatThrownBy(() -> service.updateName(7L, "a b")).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(mapper);
    }
    @Test void missingMemberIsNotUpdated() {
        assertThatThrownBy(() -> service.updateName(7L, "닉네임")).isInstanceOf(NotFoundException.class);
    }
    @Test void wrongCurrentPasswordDoesNotWrite() {
        when(mapper.findPassword(7L)).thenReturn(encoder.encode("old123"));
        assertThatThrownBy(() -> service.updatePassword(7L, request("bad123", "new123"))).hasMessageContaining("현재 비밀번호");
        verify(mapper, never()).updatePassword(any(), any(), any()); verifyNoInteractions(tokens);
    }
    @Test void passwordIsHashedAndRefreshTokensRevoked() {
        String hash = encoder.encode("old123"); when(mapper.findPassword(7L)).thenReturn(hash);
        when(mapper.updatePassword(eq(7L), eq(hash), anyString())).thenAnswer(invocation -> {
            assertThat(encoder.matches("new123", invocation.getArgument(2))).isTrue(); return 1;
        });
        service.updatePassword(7L, request("old123", "new123")); verify(tokens).deleteToken(7L);
    }
    @Test void socialAccountCannotChangePassword() {
        assertThatThrownBy(() -> service.updatePassword(7L, request("old123", "new123"))).hasMessageContaining("소셜");
        verify(mapper, never()).updatePassword(any(), any(), any());
    }
    @Test void sameAndInvalidPasswordAreRejected() {
        when(mapper.findPassword(7L)).thenReturn(encoder.encode("old123"));
        assertThatThrownBy(() -> service.updatePassword(7L, request("old123", "old123"))).hasMessageContaining("다른 비밀번호");
        assertThatThrownBy(() -> service.updatePassword(7L, request("old123", "short"))).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(tokens);
    }
    @Test void concurrentPasswordChangeIsNotOverwritten() {
        when(mapper.findPassword(7L)).thenReturn(encoder.encode("old123"));
        assertThatThrownBy(() -> service.updatePassword(7L, request("old123", "new123"))).hasMessageContaining("이미 변경");
        verifyNoInteractions(tokens);
    }
    @Test void invalidImageNeverReachesStorage() {
        assertThatThrownBy(() -> service.updatePhoto(7L, new MockMultipartFile("imageFile", "x.png", "image/png", "not image".getBytes()))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.updatePhoto(7L, new MockMultipartFile("imageFile", "x.svg", "image/svg+xml", "<svg/>".getBytes()))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.updatePhoto(7L, new MockMultipartFile("imageFile", "x.png", "image/png", new byte[5 * 1024 * 1024 + 1]))).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(files);
    }
    @Test void profilePhotoUsesMemberColumns() {
        var profile = new MemberProfileResponse(); when(mapper.findProfile(7L)).thenReturn(profile);
        when(mapper.findProfileForUpdate(7L)).thenReturn(profile);
        var image = new MockMultipartFile("imageFile", "x.png", "image/png", new byte[]{(byte)137,80,78,71,13,10,26,10});
        when(files.store(image, "profile")).thenReturn(new FileSaveResult("saved.png", "https://example.com/profile/"));
        when(mapper.updatePhoto(7L, "x.png", "saved.png", "https://example.com/profile/")).thenReturn(1);
        assertThat(service.updatePhoto(7L, image)).isSameAs(profile);
    }
    @Test void resetPhotoClearsOnlyImageFields() {
        when(mapper.findProfileForUpdate(7L)).thenReturn(new MemberProfileResponse());
        when(mapper.updatePhoto(7L, null, null, null)).thenReturn(1);
        when(mapper.findProfile(7L)).thenReturn(new MemberProfileResponse());
        service.removePhoto(7L); verifyNoInteractions(files, tokens);
    }
}
