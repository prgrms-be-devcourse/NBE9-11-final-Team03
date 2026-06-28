package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.request.AdminTradeSearchReq;
import com.back.baton.domain.admin.dto.response.AdminCreditAccountRes;
import com.back.baton.domain.admin.dto.response.AdminEscrowRes;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.dto.response.AdminTradeRes;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.repository.CreditTransactionRepository;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.CreditErrorCode;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.response.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTradeCreditService {

    private final TradeRepository tradeRepository;
    private final EscrowRepository escrowRepository;
    private final CreditAccountRepository creditAccountRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    // 관리자 거래 목록을 필터 조건으로 조회한다.
    public AdminPageRes<AdminTradeRes> getTrades(AdminTradeSearchReq req, Pageable pageable) {
        Page<AdminTradeRes> trades = tradeRepository.searchAdminTrades(
                        req.status(),
                        req.buyerId(),
                        req.sellerId(),
                        req.tradeType(),
                        pageable
                )
                .map(AdminTradeRes::from);
        return AdminPageRes.from(trades);
    }

    // 관리자 거래 상세와 연결된 에스크로 상태를 함께 조회한다.
    public TradeRes getTrade(Long tradeId) {
        Trade trade = getTradeOrThrow(tradeId);
        Escrow escrow = getEscrowByTradeIdOrThrow(tradeId);
        return TradeRes.of(trade, escrow);
    }

    // 거래 ID로 에스크로 상세 정보를 조회한다.
    public AdminEscrowRes getEscrow(Long tradeId) {
        return AdminEscrowRes.from(getEscrowByTradeIdOrThrow(tradeId));
    }

    // 유저의 크레딧 계좌 잔액 정보를 조회한다.
    public AdminCreditAccountRes getCreditAccount(Long userId) {
        CreditAccount account = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
        return AdminCreditAccountRes.from(account);
    }

    // 유저의 크레딧 거래 내역을 페이지로 조회한다.
    public AdminPageRes<CreditTransactionRes> getCreditTransactions(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        Page<CreditTransactionRes> transactions = creditTransactionRepository.findByUserIdOrderByIdDesc(userId, pageable)
                .map(CreditTransactionRes::from);
        return AdminPageRes.from(transactions);
    }

    private Trade getTradeOrThrow(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));
    }

    private Escrow getEscrowByTradeIdOrThrow(Long tradeId) {
        return escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
    }

}
