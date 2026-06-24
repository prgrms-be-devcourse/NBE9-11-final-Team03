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

        // 경로 탐색 방지: 원본 파일명에서 경로 구분자 이후 순수 파일명만 추출
        String safeFileName = extractFileName(req.fileName());

        // key 생성은 도메인 책임 (재능별 폴더 prefix + UUID + 원본 파일명)
        String key = keyPrefix(talentId) + UUID.randomUUID() + "-" + safeFileName;

        // S3 서명은 공통 서비스에 위임
        String uploadUrl = s3Service.generatePresignedPutUrl(key);

        return PresignedUrlRes.of(uploadUrl, key);
    }

    // 첨부 저장 (본인 재능만)
    @Transactional
    public AttachmentRes saveAttachment(Long talentId, Long authorId, AttachmentSaveReq req) {
        Talent talent = getActiveTalent(talentId);
        validateOwner(talent, authorId);

        String url = req.url();

        // S3 key면 반드시 본인 재능 경로여야 함.
        // 남의 재능 key를 등록해 조회 시 presigned로 열람하는 접근을 차단.
        if (isS3Key(url) && !url.startsWith(keyPrefix(talentId))) {
            throw new CustomException(TalentErrorCode.ATTACHMENT_FORBIDDEN);
        }

        TalentAttachment attachment = TalentAttachment.create(talent, url, req.description());
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

        String url = attachment.getUrl();

        // DB 레코드 먼저 삭제 (S3 삭제 실패가 DB 정합성을 깨지 않도록)
        talentAttachmentRepository.delete(attachment);

        // S3 업로드분만 실제 객체 삭제 (외부 링크는 S3 객체가 아니므로 스킵)
        if (isS3Key(url)) {
            s3Service.deleteObject(url);
        }
    }

    // 공통 헬퍼

    // 재능별 S3 key prefix - key 생성과 BOLA 검증이 같은 규칙을 공유
    private String keyPrefix(Long talentId) {
        return "talents/" + talentId + "/";
    }

    // 외부 링크(http/https)가 아니면 S3 key로 간주
    private boolean isS3Key(String url) {
        return !url.startsWith("http://") && !url.startsWith("https://");
    }

    // 경로 탐색 방지: '/' 또는 '\' 이후의 순수 파일명만 반환
    private String extractFileName(String rawFileName) {
        int idx = Math.max(rawFileName.lastIndexOf('/'), rawFileName.lastIndexOf('\\'));
        return rawFileName.substring(idx + 1);
    }

    // 저장된 url을 표시용 URL로 변환
    // 외부 링크는 그대로, S3 key는 presigned GET으로 변환
    private String toDisplayUrl(String url) {
        if (isS3Key(url)) {
            return s3Service.generatePresignedGetUrl(url);
        }
        return url;
    }

    // 공통 검증 (존재 -> 삭제 -> 소유권)
    private Talent getActiveTalent(Long talentId) {
        return talentRepository.findByIdAndDeletedAtIsNull(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
    }

    private void validateOwner(Talent talent, Long authorId) {
        if (!Objects.equals(talent.getAuthorId(), authorId)) {
            throw new CustomException(TalentErrorCode.ATTACHMENT_FORBIDDEN);
        }
    }
}