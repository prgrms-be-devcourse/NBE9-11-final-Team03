package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.talent.dto.request.AttachmentSaveReq;
import com.back.baton.domain.talent.dto.request.PresignedUrlReq;
import com.back.baton.domain.talent.dto.response.AttachmentRes;
import com.back.baton.domain.talent.dto.response.PresignedUrlRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentAttachment;
import com.back.baton.domain.talent.repository.TalentAttachmentRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import com.back.baton.global.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TalentAttachmentServiceTest {

    @InjectMocks TalentAttachmentService talentAttachmentService;
    @Mock TalentAttachmentRepository talentAttachmentRepository;
    @Mock TalentRepository talentRepository;
    @Mock S3Service s3Service;

    // ===== Presigned URL 발급 =====

    @Test
    @DisplayName("본인 재능이면 presigned URL과 key를 발급한다")
    void createPresignedUrl_success() {
        Long talentId = 1L, authorId = 7L;
        Talent talent = talent(authorId);
        ReflectionTestUtils.setField(talent, "id", talentId);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(s3Service.generatePresignedPutUrl(anyString()))
                .willReturn("https://bucket.s3.ap-northeast-2.amazonaws.com/signed");

        var req = new PresignedUrlReq("photo.png", "image/png");

        PresignedUrlRes res = talentAttachmentService.createPresignedUrl(talentId, authorId, req);

        assertThat(res.uploadUrl()).startsWith("https://");
        assertThat(res.key()).startsWith("talents/1/");
        assertThat(res.key()).endsWith("-photo.png");
        then(s3Service).should().generatePresignedPutUrl(anyString());
    }

    @Test
    @DisplayName("남의 재능에 발급 시도하면 ATTACHMENT_FORBIDDEN(403), S3 호출 안 함")
    void createPresignedUrl_forbidden() {
        given(talentRepository.findById(1L)).willReturn(Optional.of(talent(7L)));
        var req = new PresignedUrlReq("photo.png", "image/png");

        assertErrorCode(() -> talentAttachmentService.createPresignedUrl(1L, 2L, req),
                TalentErrorCode.ATTACHMENT_FORBIDDEN);
        then(s3Service).should(never()).generatePresignedPutUrl(anyString());
    }

    @Test
    @DisplayName("없는 재능이면 TALENT_NOT_FOUND(404)")
    void createPresignedUrl_talentNotFound() {
        given(talentRepository.findById(99L)).willReturn(Optional.empty());
        var req = new PresignedUrlReq("photo.png", "image/png");

        assertErrorCode(() -> talentAttachmentService.createPresignedUrl(99L, 7L, req),
                TalentErrorCode.TALENT_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 재능이면 TALENT_NOT_FOUND(404) - 소유권 검사 전에 막힌다")
    void createPresignedUrl_deletedTalent() {
        Talent talent = talent(7L);
        talent.softDelete();
        given(talentRepository.findById(1L)).willReturn(Optional.of(talent));
        var req = new PresignedUrlReq("photo.png", "image/png");

        assertErrorCode(() -> talentAttachmentService.createPresignedUrl(1L, 7L, req),
                TalentErrorCode.TALENT_NOT_FOUND);
    }

    // ===== 첨부 저장 =====

    @Test
    @DisplayName("본인 재능이면 첨부를 저장하고 응답을 반환한다")
    void saveAttachment_success() {
        Long talentId = 1L, authorId = 7L;
        Talent talent = talent(authorId);
        ReflectionTestUtils.setField(talent, "id", talentId);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(talentAttachmentRepository.save(any(TalentAttachment.class)))
                .willAnswer(inv -> inv.getArgument(0));

        var req = new AttachmentSaveReq("talents/1/uuid-photo.png", "샘플 이미지");

        AttachmentRes res = talentAttachmentService.saveAttachment(talentId, authorId, req);

        assertThat(res.url()).isEqualTo("talents/1/uuid-photo.png");
        assertThat(res.description()).isEqualTo("샘플 이미지");
        assertThat(res.talentId()).isEqualTo(talentId);
        then(talentAttachmentRepository).should().save(any(TalentAttachment.class));
    }

    @Test
    @DisplayName("남의 재능에 저장 시도하면 ATTACHMENT_FORBIDDEN(403), 저장 안 함")
    void saveAttachment_forbidden() {
        given(talentRepository.findById(1L)).willReturn(Optional.of(talent(7L)));
        var req = new AttachmentSaveReq("url", "desc");

        assertErrorCode(() -> talentAttachmentService.saveAttachment(1L, 2L, req),
                TalentErrorCode.ATTACHMENT_FORBIDDEN);
        then(talentAttachmentRepository).should(never()).save(any());
    }

    // ===== 목록 조회 =====

    @Test
    @DisplayName("재능의 첨부 목록을 id순으로 반환한다 (공개)")
    void getAttachments_success() {
        Long talentId = 1L;
        Talent talent = talent(7L);
        ReflectionTestUtils.setField(talent, "id", talentId);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(talentAttachmentRepository.findByTalentIdOrderByIdAsc(talentId))
                .willReturn(List.of(
                        attachment(talent, "url1", "첫번째"),
                        attachment(talent, "url2", "두번째")
                ));

        List<AttachmentRes> res = talentAttachmentService.getAttachments(talentId);

        assertThat(res).hasSize(2);
        assertThat(res).extracting(AttachmentRes::url).containsExactly("url1", "url2");
    }

    @Test
    @DisplayName("없는 재능의 첨부 목록 조회는 TALENT_NOT_FOUND(404)")
    void getAttachments_talentNotFound() {
        given(talentRepository.findById(99L)).willReturn(Optional.empty());

        assertErrorCode(() -> talentAttachmentService.getAttachments(99L),
                TalentErrorCode.TALENT_NOT_FOUND);
    }

    // ===== 삭제 =====

    @Test
    @DisplayName("본인 재능의 첨부를 삭제한다")
    void deleteAttachment_success() {
        Long talentId = 1L, attachmentId = 10L, authorId = 7L;
        Talent talent = talent(authorId);
        ReflectionTestUtils.setField(talent, "id", talentId);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));

        TalentAttachment attachment = attachment(talent, "url", "desc");
        given(talentAttachmentRepository.findById(attachmentId)).willReturn(Optional.of(attachment));

        talentAttachmentService.deleteAttachment(talentId, attachmentId, authorId);

        then(talentAttachmentRepository).should().delete(attachment);
    }

    @Test
    @DisplayName("남의 재능 첨부 삭제 시도하면 ATTACHMENT_FORBIDDEN(403)")
    void deleteAttachment_forbidden() {
        Talent talent = talent(7L);
        ReflectionTestUtils.setField(talent, "id", 1L);
        given(talentRepository.findById(1L)).willReturn(Optional.of(talent));

        assertErrorCode(() -> talentAttachmentService.deleteAttachment(1L, 10L, 2L),
                TalentErrorCode.ATTACHMENT_FORBIDDEN);
        then(talentAttachmentRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("없는 첨부 id면 ATTACHMENT_NOT_FOUND(404)")
    void deleteAttachment_attachmentNotFound() {
        Long talentId = 1L, authorId = 7L;
        Talent talent = talent(authorId);
        ReflectionTestUtils.setField(talent, "id", talentId);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(talentAttachmentRepository.findById(99L)).willReturn(Optional.empty());

        assertErrorCode(() -> talentAttachmentService.deleteAttachment(talentId, 99L, authorId),
                TalentErrorCode.ATTACHMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("경로 talentId와 첨부의 talentId가 다르면 ATTACHMENT_NOT_FOUND(404)")
    void deleteAttachment_talentMismatch() {
        Long pathTalentId = 1L, attachmentId = 10L, authorId = 7L;

        Talent pathTalent = talent(authorId);
        ReflectionTestUtils.setField(pathTalent, "id", pathTalentId);
        given(talentRepository.findById(pathTalentId)).willReturn(Optional.of(pathTalent));

        Talent otherTalent = talent(authorId);
        ReflectionTestUtils.setField(otherTalent, "id", 2L);
        TalentAttachment attachment = attachment(otherTalent, "url", "desc");
        given(talentAttachmentRepository.findById(attachmentId)).willReturn(Optional.of(attachment));

        assertErrorCode(() -> talentAttachmentService.deleteAttachment(pathTalentId, attachmentId, authorId),
                TalentErrorCode.ATTACHMENT_NOT_FOUND);
        then(talentAttachmentRepository).should(never()).delete(any());
    }

    // ===== 헬퍼 =====

    private Talent talent(Long authorId) {
        return Talent.create(authorId, mock(Category.class), "제목", "내용", 2, 100);
    }

    private TalentAttachment attachment(Talent talent, String url, String description) {
        return TalentAttachment.create(talent, url, description);
    }

    private void assertErrorCode(Runnable callable, TalentErrorCode expected) {
        assertThatThrownBy(callable::run)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(expected));
    }
}