package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;

import java.util.List;

public interface CreditTransactionRepositoryCustom {

    List<CreditTransactionRes> findHistory(Long userId, CreditTransactionSearchReq req, Long cursor, int size);
}