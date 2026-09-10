package com.kh.wellness.member.model.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.member.model.dao.MemberProfileMapper;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import com.kh.wellness.token.model.dao.TokenMapper;

@ExtendWith(MockitoExtension.class)
class MemberProfilePhotoRemovalTest {
    @Mock MemberProfileMapper mapper;
    @Mock S3Service storage;
    MemberProfileService service;
    final MemberProfilePhotoTransactionTest.TestTransactionManager manager = new MemberProfilePhotoTransactionTest.TestTransactionManager();
    static final String NAME = "11111111-1111-4111-8111-111111111111.png";

    @BeforeEach void setup() {
        service = new MemberProfileService(mapper, mock(PasswordEncoder.class), mock(TokenMapper.class), mock(FileService.class), storage);
        ReflectionTestUtils.setField(service, "bucketName", "profile-test");
        ReflectionTestUtils.setField(service, "region", "ap-northeast-2");
        var profile = new MemberProfileResponse();
        profile.setImgPath("https://profile-test.s3.ap-northeast-2.amazonaws.com/profile/"); profile.setSaveName(NAME);
        when(mapper.findProfileForUpdate(7L)).thenReturn(profile);
    }
    void success() {
        when(mapper.updatePhoto(7L, null, null, null)).thenReturn(1);
        when(mapper.findProfile(7L)).thenReturn(new MemberProfileResponse());
    }
    @Test void resetCommitsBeforeDeletingPreviousPhoto() {
        success();
        new TransactionTemplate(manager).execute(status -> {
            assertThat(service.removePhoto(7L).getImgPath()).isNull();
            verifyNoInteractions(storage);
            return null;
        });
        verify(storage).deleteFile("profile/" + NAME);
        var order = inOrder(mapper);
        order.verify(mapper).findProfileForUpdate(7L);
        order.verify(mapper).updatePhoto(7L, null, null, null);
    }
    @Test void resetUpdateFailureKeepsPreviousPhoto() {
        assertThatThrownBy(() -> new TransactionTemplate(manager).execute(status -> service.removePhoto(7L)))
            .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(storage);
    }
    @Test void resetOuterRollbackKeepsPreviousPhoto() {
        success();
        new TransactionTemplate(manager).execute(status -> { service.removePhoto(7L); status.setRollbackOnly(); return null; });
        verifyNoInteractions(storage);
    }
    @Test void resetCommitFailureKeepsPreviousPhoto() {
        success(); manager.failCommit = true; manager.setRollbackOnCommitFailure(true);
        assertThatThrownBy(() -> new TransactionTemplate(manager).execute(status -> service.removePhoto(7L)))
            .isInstanceOf(TransactionSystemException.class);
        verifyNoInteractions(storage);
    }
    @Test void resetMissingImageDoesNotCallStorage() {
        success(); when(mapper.findProfileForUpdate(7L)).thenReturn(new MemberProfileResponse());
        new TransactionTemplate(manager).execute(status -> service.removePhoto(7L));
        verifyNoInteractions(storage);
    }
    @Test void missingMemberDoesNotUpdateOrDelete() {
        when(mapper.findProfileForUpdate(7L)).thenReturn(null);
        assertThatThrownBy(() -> new TransactionTemplate(manager).execute(status -> service.removePhoto(7L)))
            .isInstanceOf(NotFoundException.class);
        verify(mapper, never()).updatePhoto(any(), any(), any(), any());
        verifyNoInteractions(storage);
    }
}
