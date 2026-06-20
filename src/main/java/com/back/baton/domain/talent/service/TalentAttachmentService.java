package com.back.baton.domain.talent.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentAttachmentService {

    private final TalentRepository talentRepository;
    private final TalentAttachmentRepository talentAttachmentRepository;
    private final S3Service s3Service;   // S3Presigner 직접 사용 대신 공통 서비스 위임

    // presigned URL 발급 (본인 재능만)
    public PresignedUrlRes createPresignedUrl(Long talentId, Long authorId, PresignedUrlReq req) {
        Talent talent = getActiveTalent(talentId);
        validateOwner(talent, authorId);

        // key 생성은 도메인 책임 (재능별 폴더 prefix + UUID + 원본 파일명)
        String key = "talents/" + talentId + "/" + UUID.randomUUID() + "-" + req.fileName();

        // S3 서명은 공통 서비스에 위임
        String uploadUrl = s3Service.generatePresignedPutUrl(key);

        return PresignedUrlRes.of(uploadUrl, key);
    }

    // 첨부 저장 (본인 재능만)
    @Transactional
    public AttachmentRes saveAttachment(Long talentId, Long authorId, AttachmentSaveReq req) {
        Talent talent = getActiveTalent(talentId);
        validateOwner(talent, authorId);

        TalentAttachment attachment = TalentAttachment.create(talent, req.url(), req.description());
        TalentAttachment saved = talentAttachmentRepository.save(attachment);

        // 저장 응답도 표시용 URL로 통일 (목록 조회와 동일 의미)
        return AttachmentRes.of(saved, toDisplayUrl(saved.getUrl()));
    }

    // 첨부 목록 조회 (공개 - 소유권 검사 없음)
    public List<AttachmentRes> getAttachments(Long talentId) {
        getActiveTalent(talentId); // 존재/삭제 검증만
        return talentAttachmentRepository.findByTalentIdOrderByIdAsc(talentId).stream()
                .map(attachment -> AttachmentRes.of(attachment, toDisplayUrl(attachment.getUrl())))
                .toList();
    }

    // 첨부 삭제 (본인 재능만)
    @Transactional
    public void deleteAttachment(Long talentId, Long attachmentId, Long authorId) {
        Talent talent = getActiveTalent(talentId);
        validateOwner(talent, authorId);

        TalentAttachment attachment = talentAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.ATTACHMENT_NOT_FOUND));

        // 경로 talentId와 첨부 실제 소속이 다르면 없는 것으로 취급
        if (!Objects.equals(attachment.getTalent().getId(), talentId)) {
            throw new CustomException(TalentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // TODO: S3 객체 삭제(deleteObject)는 공통 S3 Service에 메서드 추가 - 지금은 DB 레코드만 제거
        talentAttachmentRepository.delete(attachment);
    }

    // 저장된 url을 표시용 URL로 변환
    // http(s)://로 시작하면 외부 링크 -> 그대로 아니면 S3 key  presigned GET 변환
    private String toDisplayUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return s3Service.generatePresignedGetUrl(url);
    }

    // 공통 검증 (존재 -> 삭제 -> 소유권)

    private Talent getActiveTalent(Long talentId) {
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
        if (talent.isDeleted()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }
        return talent;
    }

    private void validateOwner(Talent talent, Long authorId) {
        if (!Objects.equals(talent.getAuthorId(), authorId)) {
            throw new CustomException(TalentErrorCode.ATTACHMENT_FORBIDDEN);
        }
    }
}