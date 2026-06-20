package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.entity.TalentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TalentAttachmentRepository extends JpaRepository<TalentAttachment, Long> {

    // 특정 재능의 첨부 목록
    List<TalentAttachment> findByTalentIdOrderByIdAsc(Long talentId);
}