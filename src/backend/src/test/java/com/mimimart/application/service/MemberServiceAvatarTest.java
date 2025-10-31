package com.mimimart.application.service;

import com.mimimart.domain.member.exception.*;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.storage.S3StorageService;
import com.mimimart.shared.valueobject.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 會員頭貼功能測試
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("會員頭貼功能測試")
class MemberServiceAvatarTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setEmail("test@example.com");
        testMember.setPasswordHash("hashed_password");
        testMember.setName("測試會員");
        testMember.setStatus(MemberStatus.ACTIVE);
        testMember.setEmailVerified(true);
        testMember.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("上傳頭貼 - 成功")
    void uploadAvatar_Success() {
        // Arrange
        Long memberId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String s3Key = "avatars/1/20241031120000_test.jpg";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(s3StorageService.uploadAvatar(eq(memberId), any(MultipartFile.class))).thenReturn(s3Key);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // Act
        Member result = memberService.uploadAvatar(memberId, file);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAvatarS3Key()).isEqualTo(s3Key);
        assertThat(result.getAvatarUrl()).isEqualTo(s3Key);
        assertThat(result.getAvatarUpdatedAt()).isNotNull();

        verify(memberRepository).findById(memberId);
        verify(s3StorageService).uploadAvatar(eq(memberId), any(MultipartFile.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("上傳頭貼 - 檔案為空")
    void uploadAvatar_EmptyFile() {
        // Arrange
        Long memberId = 1L;
        MockMultipartFile emptyFile = new MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        assertThatThrownBy(() -> memberService.uploadAvatar(memberId, emptyFile))
                .isInstanceOf(InvalidAvatarException.class)
                .hasMessageContaining("檔案不能為空");

        verify(memberRepository, never()).findById(any());
        verify(s3StorageService, never()).uploadAvatar(any(), any());
    }

    @Test
    @DisplayName("上傳頭貼 - 檔案過大")
    void uploadAvatar_FileTooLarge() {
        // Arrange
        Long memberId = 1L;
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "avatar",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        assertThatThrownBy(() -> memberService.uploadAvatar(memberId, largeFile))
                .isInstanceOf(InvalidAvatarException.class)
                .hasMessageContaining("檔案大小超過限制");

        verify(memberRepository, never()).findById(any());
        verify(s3StorageService, never()).uploadAvatar(any(), any());
    }

    @Test
    @DisplayName("上傳頭貼 - 不支援的檔案格式")
    void uploadAvatar_InvalidFileType() {
        // Arrange
        Long memberId = 1L;
        MockMultipartFile invalidFile = new MockMultipartFile(
                "avatar",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Act & Assert
        assertThatThrownBy(() -> memberService.uploadAvatar(memberId, invalidFile))
                .isInstanceOf(InvalidAvatarException.class)
                .hasMessageContaining("不支援的檔案格式");

        verify(memberRepository, never()).findById(any());
        verify(s3StorageService, never()).uploadAvatar(any(), any());
    }

    @Test
    @DisplayName("上傳頭貼 - 會員不存在")
    void uploadAvatar_MemberNotFound() {
        // Arrange
        Long memberId = 999L;
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> memberService.uploadAvatar(memberId, file))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("會員不存在");

        verify(memberRepository).findById(memberId);
        verify(s3StorageService, never()).uploadAvatar(any(), any());
    }

    @Test
    @DisplayName("上傳頭貼 - 替換現有頭貼")
    void uploadAvatar_ReplaceExisting() {
        // Arrange
        Long memberId = 1L;
        String oldS3Key = "avatars/1/old_avatar.jpg";
        testMember.setAvatarS3Key(oldS3Key);

        MockMultipartFile newFile = new MockMultipartFile(
                "avatar",
                "new.jpg",
                "image/jpeg",
                "new content".getBytes()
        );
        String newS3Key = "avatars/1/20241031120000_new.jpg";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(s3StorageService.uploadAvatar(eq(memberId), any(MultipartFile.class))).thenReturn(newS3Key);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // Act
        memberService.uploadAvatar(memberId, newFile);

        // Assert
        verify(s3StorageService).deleteAvatar(oldS3Key);
        verify(s3StorageService).uploadAvatar(eq(memberId), any(MultipartFile.class));
    }

    @Test
    @DisplayName("取得頭貼資料 - 成功")
    void getAvatarData_Success() {
        // Arrange
        Long memberId = 1L;
        Long requesterMemberId = 1L;
        String s3Key = "avatars/1/avatar.jpg";
        byte[] avatarData = "image data".getBytes();

        testMember.setAvatarS3Key(s3Key);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(s3StorageService.downloadAvatar(s3Key)).thenReturn(avatarData);

        // Act
        byte[] result = memberService.getAvatarData(memberId, requesterMemberId);

        // Assert
        assertThat(result).isEqualTo(avatarData);
        verify(memberRepository).findById(memberId);
        verify(s3StorageService).downloadAvatar(s3Key);
    }

    @Test
    @DisplayName("取得頭貼資料 - 無權訪問他人頭貼")
    void getAvatarData_Unauthorized() {
        // Arrange
        Long memberId = 1L;
        Long requesterMemberId = 2L; // 不同會員

        // Act & Assert
        assertThatThrownBy(() -> memberService.getAvatarData(memberId, requesterMemberId))
                .isInstanceOf(UnauthorizedAvatarAccessException.class)
                .hasMessageContaining("無權訪問該會員的頭貼");

        verify(memberRepository, never()).findById(any());
        verify(s3StorageService, never()).downloadAvatar(any());
    }

    @Test
    @DisplayName("取得頭貼資料 - 會員尚未上傳頭貼")
    void getAvatarData_NoAvatar() {
        // Arrange
        Long memberId = 1L;
        Long requesterMemberId = 1L;
        testMember.setAvatarS3Key(null); // 沒有頭貼

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));

        // Act & Assert
        assertThatThrownBy(() -> memberService.getAvatarData(memberId, requesterMemberId))
                .isInstanceOf(AvatarNotFoundException.class)
                .hasMessageContaining("該會員尚未上傳頭貼");

        verify(memberRepository).findById(memberId);
        verify(s3StorageService, never()).downloadAvatar(any());
    }

    @Test
    @DisplayName("取得頭貼 Content-Type - 成功")
    void getAvatarContentType_Success() {
        // Arrange
        Long memberId = 1L;
        String s3Key = "avatars/1/avatar.jpg";
        String contentType = "image/jpeg";

        testMember.setAvatarS3Key(s3Key);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(s3StorageService.getContentType(s3Key)).thenReturn(contentType);

        // Act
        String result = memberService.getAvatarContentType(memberId);

        // Assert
        assertThat(result).isEqualTo(contentType);
        verify(memberRepository).findById(memberId);
        verify(s3StorageService).getContentType(s3Key);
    }
}
