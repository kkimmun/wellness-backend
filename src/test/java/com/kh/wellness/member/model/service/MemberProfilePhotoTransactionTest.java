package com.kh.wellness.member.model.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.member.model.dao.MemberProfileMapper;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import com.kh.wellness.token.model.dao.TokenMapper;

@ExtendWith(MockitoExtension.class)
class MemberProfilePhotoTransactionTest {
    @Mock MemberProfileMapper mapper;
    @Mock FileService files;
    @Mock S3Service storage;
    @Mock PasswordEncoder encoder;
    @Mock TokenMapper tokens;
    MemberProfileService service;
    static final String OLD_NAME = "11111111-1111-4111-8111-111111111111.png";
    static final String BASE_URL = "https://profile-test.s3.ap-northeast-2.amazonaws.com/profile/";
    final TestTransactionManager manager = new TestTransactionManager();
    final MockMultipartFile image = new MockMultipartFile("imageFile", "new.png", "image/png",
            new byte[]{(byte)137,80,78,71,13,10,26,10});

    @BeforeEach void setup() {
        service = new MemberProfileService(mapper, encoder, tokens, files, storage);
        ReflectionTestUtils.setField(service, "bucketName", "profile-test");
        ReflectionTestUtils.setField(service, "region", "ap-northeast-2");
        var old = new MemberProfileResponse();
        old.setImgPath(BASE_URL); old.setSaveName(OLD_NAME);
        when(mapper.findProfileForUpdate(7L)).thenReturn(old);
        when(files.store(image, "profile")).thenReturn(new FileSaveResult("new.png", "https://example.com/profile/"));
    }

    void successfulUpdate() {
        when(mapper.updatePhoto(7L, "new.png", "new.png", "https://example.com/profile/")).thenReturn(1);
        when(mapper.findProfile(7L)).thenReturn(new MemberProfileResponse());
    }

    @Test void commitDeletesOnlyPreviousPhotoAfterServiceReturns() {
        successfulUpdate();
        new TransactionTemplate(manager).execute(status -> {
            service.updatePhoto(7L, image);
            verifyNoInteractions(storage);
            return null;
        });
        verify(storage).deleteFile("profile/" + OLD_NAME);
        verifyNoMoreInteractions(storage);
        var order = inOrder(mapper, files);
        order.verify(mapper).findProfileForUpdate(7L);
        order.verify(files).store(image, "profile");
        order.verify(mapper).updatePhoto(7L, "new.png", "new.png", "https://example.com/profile/");
    }

    @Test void zeroUpdatedRowsRollbackAndDeleteOnlyNewObject() {
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isInstanceOf(NotFoundException.class);
        verify(storage).deleteFile("profile/new.png");
        verifyNoMoreInteractions(storage);
    }

    @Test void databaseExceptionIsPreservedAndUploadRemoved() {
        var failure = new IllegalStateException("DB update failed");
        when(mapper.updatePhoto(any(), any(), any(), any())).thenThrow(failure);
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isSameAs(failure);
        verify(storage).deleteFile("profile/new.png");
    }

    @Test void failureReadingUpdatedProfileAlsoCleansUpload() {
        successfulUpdate();
        when(mapper.findProfile(7L)).thenReturn(null);
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isInstanceOf(NotFoundException.class);
        verify(storage).deleteFile("profile/new.png");
    }

    @Test void outerRollbackAfterServiceReturnsCleansUpload() {
        successfulUpdate();
        new TransactionTemplate(manager).execute(status -> {
            service.updatePhoto(7L, image);
            verifyNoInteractions(storage);
            status.setRollbackOnly();
            return null;
        });
        verify(storage).deleteFile("profile/new.png");
    }

    @Test void commitFailureFollowedByRollbackCleansUpload() {
        successfulUpdate(); manager.failCommit = true; manager.setRollbackOnCommitFailure(true);
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isInstanceOf(TransactionSystemException.class);
        verify(storage).deleteFile("profile/new.png");
    }

    @Test void unknownTransactionOutcomeDoesNotDeletePotentiallyCommittedImage() {
        successfulUpdate(); manager.failCommit = true;
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isInstanceOf(TransactionSystemException.class);
        verifyNoInteractions(storage);
    }

    @Test void temporaryCleanupFailureIsRetried() {
        doThrow(new IllegalStateException("temporary S3 failure")).doNothing().when(storage).deleteFile("profile/new.png");
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isInstanceOf(NotFoundException.class);
        verify(storage, times(2)).deleteFile("profile/new.png");
    }

    @Test void permanentCleanupFailureDoesNotMaskDatabaseError() {
        doThrow(new IllegalStateException("S3 unavailable")).when(storage).deleteFile("profile/new.png");
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).isInstanceOf(NotFoundException.class);
        verify(storage, times(3)).deleteFile("profile/new.png");
    }

    @Test void directCallWithoutTransactionAlsoCleansFailedUpload() {
        assertThatThrownBy(() -> service.updatePhoto(7L, image)).isInstanceOf(NotFoundException.class);
        verify(storage).deleteFile("profile/new.png");
    }

    @Test void storageFailureDoesNotDeleteAnyExistingObject() {
        when(files.store(image, "profile")).thenThrow(new IllegalStateException("upload failed"));
        assertThatThrownBy(() -> new TransactionTemplate(manager)
                .execute(status -> service.updatePhoto(7L, image))).hasMessage("upload failed");
        verify(mapper, never()).updatePhoto(any(), any(), any(), any());
        verifyNoInteractions(storage);
    }

    @Test void foreignBucketOrSharedDefaultImageIsNotDeleted() {
        successfulUpdate();
        for (String url : new String[]{"https://other.s3.ap-northeast-2.amazonaws.com/profile/",
                "https://profile-test.s3.ap-northeast-2.amazonaws.com/default/"}) {
            var profile = new MemberProfileResponse(); profile.setImgPath(url); profile.setSaveName(OLD_NAME);
            when(mapper.findProfileForUpdate(7L)).thenReturn(profile);
            new TransactionTemplate(manager).execute(status -> service.updatePhoto(7L, image));
        }
        var shared = new MemberProfileResponse(); shared.setImgPath(BASE_URL); shared.setSaveName("default.png");
        when(mapper.findProfileForUpdate(7L)).thenReturn(shared);
        new TransactionTemplate(manager).execute(status -> service.updatePhoto(7L, image));
        verifyNoInteractions(storage);
    }

    @Test void commitCleanupFailureRetriesWithoutFailingSuccessfulUpdate() {
        successfulUpdate();
        doThrow(new IllegalStateException("S3 unavailable")).when(storage).deleteFile("profile/" + OLD_NAME);
        assertThatCode(() -> new TransactionTemplate(manager).execute(status -> service.updatePhoto(7L, image)))
            .doesNotThrowAnyException();
        verify(storage, times(3)).deleteFile("profile/" + OLD_NAME);
        verify(storage, never()).deleteFile("profile/new.png");
    }

    @Test void consecutiveReplacementsCleanIntermediatePhotoButKeepFinalPhoto() {
        String firstName = "22222222-2222-4222-8222-222222222222.png";
        String lastName = "33333333-3333-4333-8333-333333333333.png";
        var firstProfile = new MemberProfileResponse(); firstProfile.setImgPath(BASE_URL); firstProfile.setSaveName(firstName);
        var oldProfile = mapper.findProfileForUpdate(7L);
        when(mapper.findProfileForUpdate(7L)).thenReturn(oldProfile, firstProfile);
        when(files.store(image, "profile")).thenReturn(new FileSaveResult(firstName, BASE_URL), new FileSaveResult(lastName, BASE_URL));
        when(mapper.updatePhoto(any(), any(), any(), any())).thenReturn(1);
        when(mapper.findProfile(7L)).thenReturn(firstProfile);
        new TransactionTemplate(manager).execute(status -> {
            service.updatePhoto(7L, image);
            service.updatePhoto(7L, image);
            verifyNoInteractions(storage);
            return null;
        });
        verify(storage).deleteFile("profile/" + OLD_NAME);
        verify(storage).deleteFile("profile/" + firstName);
        verifyNoMoreInteractions(storage);
    }

    // 실제 Spring 트랜잭션 동기화 수명주기를 실행하되 DB/S3에는 접속하지 않는다.
    static class TestTransactionManager extends AbstractPlatformTransactionManager {
        boolean failCommit;
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) {
            if (failCommit) throw new TransactionSystemException("commit failed");
        }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }
}
