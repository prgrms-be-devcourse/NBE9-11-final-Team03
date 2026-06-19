package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import java.util.List;

public interface TalentRepositoryCustom {
    // 커서 이후 size+1개를 조회 (hasNext는 Service)
    List<TalentListRes> findTalentList(Long cursor, int size);

    // 검색, 필터: 동적 조건 + 커서 페이징
    List<TalentListRes> searchTalents(TalentSearchReq req, Long cursor, int size);
}