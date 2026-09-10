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
    final TestTransactionManager manager = new TestTransactionManager();
    final MockMultipartFile image = new MockMultipartFile("imageFile", "new.png", "image/png",
            new byte[]{(byte)137,80,78,71,13,10,26,10});

    @BeforeEach void setup() {
        service = new MemberProfileService(mapper, encoder, tokens, files, storage);
        var old = new MemberProfileResponse();
        old.setImgPath("https://example.com/profile/"); old.setSaveName("old.png");
        when(mapper.findProfile(7L)).thenReturn(old);
        when(files.store(image, "profile")).thenReturn(new FileSaveResult("new.png", "https://example.com/profile/"));
    }

    void successfulUpdate() {
        when(mapper.updatePhoto(7L, "new.png", "new.png", "https://example.com/profile/")).thenReturn(1);
    }

    @Test void committedUploadIsNotDeleted() {
        successfulUpdate();
        new TransactionTemplate(manager).execute(status -> service.updatePhoto(7L, image));
        verifyNoInteractions(storage);
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
        when(mapper.findProfile(7L)).thenReturn(new MemberProfileResponse()).thenReturn(null);
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
