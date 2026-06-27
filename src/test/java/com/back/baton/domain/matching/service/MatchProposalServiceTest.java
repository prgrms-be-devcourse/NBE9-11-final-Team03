package com.back.baton.domain.matching.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.chat.dto.request.TradeChatRoomCreateReq;
import com.back.baton.domain.chat.service.ChatService;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.service.EscrowService;
import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.MatchingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchProposalServiceTest {

    @Mock
    private MatchProposalRepository matchProposalRepository;

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private TradeService tradeService;

    @Mock
    private CreditService creditService;

    @Mock
    private EscrowService escrowService;

    @InjectMocks
    private MatchProposalService matchProposalService;

    @Mock
    private ChatService chatService;

    @Test
    @DisplayName("단방향 매칭 제안을 생성할 수 있다")
    void createMatchProposal() {
        Long requesterId = 1L;

        MatchProposalCreateReq req = new MatchProposalCreateReq(
                null,
                2L,
                20L,
                "재능 구매 제안드립니다."
        );

        Talent providerTalent = createTalent(req.providerTalentId(), req.providerId());

        when(talentRepository.findById(req.providerTalentId()))
                .thenReturn(Optional.of(providerTalent));

        when(matchProposalRepository.existsActiveProposal(
                requesterId,
                req.requesterTalentId(),
                req.providerTalentId(),
                List.of(
                        MatchProposalStatus.REQUESTED,
                        MatchProposalStatus.ACCEPTED
                )
        )).thenReturn(false);

        when(matchProposalRepository.saveAndFlush(any(MatchProposal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchProposalRes res = matchProposalService.createMatchProposal(requesterId, req);

        assertThat(res.providerId()).isEqualTo(req.providerId());
        assertThat(res.requesterId()).isEqualTo(requesterId);
        assertThat(res.providerTalentId()).isEqualTo(req.providerTalentId());
        assertThat(res.requesterTalentId()).isNull();
        assertThat(res.requestMessage()).isEqualTo(req.requestMessage());
        assertThat(res.status()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(res.respondedAt()).isNull();

        verify(talentRepository).findById(req.providerTalentId());
        verify(matchProposalRepository).existsActiveProposal(
                requesterId,
                req.requesterTalentId(),
                req.providerTalentId(),
                List.of(
                        MatchProposalStatus.REQUESTED,
                        MatchProposalStatus.ACCEPTED
                )
        );
        verify(matchProposalRepository).saveAndFlush(any(MatchProposal.class));
    }

    @Test
    @DisplayName("교환 매칭 제안을 생성할 수 있다")
    void createMatchProposal_swap() {
        Long requesterId = 1L;
        Long providerId = 2L;

        Talent requesterTalent = createTalent(10L, requesterId);
        Talent providerTalent = createTalent(20L, providerId);

        MatchProposalCreateReq req = new MatchProposalCreateReq(
                requesterTalent.getId(),
                providerId,
                providerTalent.getId(),
                "재능 교환 제안드립니다."
        );

        when(talentRepository.findById(providerTalent.getId()))
                .thenReturn(Optional.of(providerTalent));
        when(talentRepository.findById(requesterTalent.getId()))
                .thenReturn(Optional.of(requesterTalent));
        when(matchProposalRepository.existsByActiveSwapPairKey(
                MatchProposal.createActiveSwapPairKey(
                        requesterTalent.getId(),
                        providerTalent.getId()
                )
        )).thenReturn(false);
        when(matchProposalRepository.saveAndFlush(any(MatchProposal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchProposalRes res = matchProposalService.createMatchProposal(requesterId, req);

        assertThat(res.providerId()).isEqualTo(providerId);
        assertThat(res.requesterId()).isEqualTo(requesterId);
        assertThat(res.providerTalentId()).isEqualTo(providerTalent.getId());
        assertThat(res.requesterTalentId()).isEqualTo(requesterTalent.getId());
        assertThat(res.requestMessage()).isEqualTo(req.requestMessage());
        assertThat(res.status()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(res.respondedAt()).isNull();

        verify(matchProposalRepository).existsByActiveSwapPairKey(
                MatchProposal.createActiveSwapPairKey(
                        requesterTalent.getId(),
                        providerTalent.getId()
                )
        );
        verify(matchProposalRepository).saveAndFlush(any(MatchProposal.class));
    }

    @Test
    @DisplayName("자기 자신에게 매칭을 제안할 수 없다")
    void createMatchProposal_selfProposal() {
        Long requesterId = 1L;

        MatchProposalCreateReq req = new MatchProposalCreateReq(
                null,
                1L,
                20L,
                "재능 구매 제안드립니다."
        );

        Talent providerTalent = createTalent(req.providerTalentId(), req.providerId());

        when(talentRepository.findById(req.providerTalentId()))
                .thenReturn(Optional.of(providerTalent));

        assertThatThrownBy(() -> matchProposalService.createMatchProposal(requesterId, req))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository, never()).save(any(MatchProposal.class));
    }

    @Test
    @DisplayName("다른 사람의 재능으로는 매칭 제안을 생성할 수 없다")
    void createMatchProposal_requesterTalentNotOwned() {
        Long requesterId = 1L;

        MatchProposalCreateReq req = new MatchProposalCreateReq(
                10L,
                2L,
                20L,
                "재능 교환 제안드립니다."
        );

        Talent requesterTalent = createTalent(req.requesterTalentId(), 999L);
        Talent providerTalent = createTalent(req.providerTalentId(), req.providerId());

        when(talentRepository.findById(req.providerTalentId()))
                .thenReturn(Optional.of(providerTalent));
        when(talentRepository.findById(req.requesterTalentId()))
                .thenReturn(Optional.of(requesterTalent));

        assertThatThrownBy(() -> matchProposalService.createMatchProposal(requesterId, req))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository, never()).save(any(MatchProposal.class));
    }

    @Test
    @DisplayName("동일 조건의 진행 중인 매칭 제안이 있으면 중복 생성할 수 없다")
    void createMatchProposal_duplicatedProposal() {
        Long requesterId = 1L;

        MatchProposalCreateReq req = new MatchProposalCreateReq(
                null,
                2L,
                20L,
                "재능 구매 제안드립니다."
        );

        Talent providerTalent = createTalent(req.providerTalentId(), req.providerId());

        when(talentRepository.findById(req.providerTalentId()))
                .thenReturn(Optional.of(providerTalent));

        when(matchProposalRepository.existsActiveProposal(
                requesterId,
                req.requesterTalentId(),
                req.providerTalentId(),
                List.of(
                        MatchProposalStatus.REQUESTED,
                        MatchProposalStatus.ACCEPTED
                )
        )).thenReturn(true);

        assertThatThrownBy(() -> matchProposalService.createMatchProposal(requesterId, req))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository).existsActiveProposal(
                requesterId,
                req.requesterTalentId(),
                req.providerTalentId(),
                List.of(
                        MatchProposalStatus.REQUESTED,
                        MatchProposalStatus.ACCEPTED
                )
        );
        verify(matchProposalRepository, never()).save(any(MatchProposal.class));
    }

    @Test
    @DisplayName("상대방이 이미 보낸 교환 요청이 있으면 역방향 교환 요청을 생성할 수 없다")
    void createMatchProposal_rejectReverseSwapProposal() {
        Long requesterId = 1L;
        Long providerId = 2L;

        Talent requesterTalent = createTalent(10L, requesterId);
        Talent providerTalent = createTalent(20L, providerId);

        MatchProposalCreateReq reverseReq = new MatchProposalCreateReq(
                providerTalent.getId(),
                requesterId,
                requesterTalent.getId(),
                "역방향 교환 요청입니다."
        );

        when(talentRepository.findById(requesterTalent.getId()))
                .thenReturn(Optional.of(requesterTalent));
        when(talentRepository.findById(providerTalent.getId()))
                .thenReturn(Optional.of(providerTalent));
        when(matchProposalRepository.existsByActiveSwapPairKey(
                MatchProposal.createActiveSwapPairKey(
                        reverseReq.requesterTalentId(),
                        reverseReq.providerTalentId()
                )
        )).thenReturn(true);

        assertThatThrownBy(() -> matchProposalService.createMatchProposal(providerId, reverseReq))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MatchingErrorCode.DUPLICATED_MATCHING_PROPOSAL);

        verify(matchProposalRepository, never()).save(any(MatchProposal.class));
    }

    @Test
    @DisplayName("판매자는 요청받은 매칭 제안을 수락할 수 있다")
    void acceptMatchProposal() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;
        Long providerTalentId = 20L;
        Long tradeId = 100L;
        int creditPrice = 100;

        MatchProposal matchProposal = createPurchaseProposal(
                providerTalentId,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                creditPrice
        );

        ReflectionTestUtils.setField(matchProposal, "id", proposalId);

        Talent providerTalent = createTalent(providerTalentId, providerId);

        Trade trade = Trade.create(
                proposalId,
                null,
                providerTalentId,
                requesterId,
                providerId,
                creditPrice,
                TradeType.PURCHASE
        );
        ReflectionTestUtils.setField(trade, "id", tradeId);

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        when(talentRepository.findById(providerTalentId))
                .thenReturn(Optional.of(providerTalent));

        when(tradeService.createPurchaseTrade(matchProposal)).thenReturn(trade);

        when(matchProposalRepository.save(matchProposal))
                .thenReturn(matchProposal);

        MatchProposalRes res = matchProposalService.acceptMatchProposal(
                proposalId,
                providerId
        );

        assertThat(res.status()).isEqualTo(MatchProposalStatus.ACCEPTED);
        assertThat(res.respondedAt()).isNotNull();

        verify(matchProposalRepository).findById(proposalId);
        verify(talentRepository).findById(providerTalentId);
        verify(tradeService).createPurchaseTrade(matchProposal);
        verify(creditService).holdForEscrow(
                requesterId,
                creditPrice,
                tradeId
        );
        verify(escrowService).create(
                tradeId,
                requesterId,
                providerId,
                creditPrice
        );

        verify(chatService).getOrCreateTransactionRoom(TradeChatRoomCreateReq.from(trade));

        verify(matchProposalRepository).save(matchProposal);
    }

    @Test
    @DisplayName("양방향 교환 제안 수락은 Trade/Credit/Escrow 구현 전까지 지원하지 않는다")
    void acceptMatchProposal_swapNotImplemented() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;

        MatchProposal matchProposal = MatchProposal.create(
                20L,
                10L,
                requesterId,
                providerId,
                "재능 교환 제안드립니다.",
                150,
                100,
                MatchProposal.createActiveSwapPairKey(10L, 20L)
        );

        ReflectionTestUtils.setField(matchProposal, "id", proposalId);

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        assertThatThrownBy(() -> matchProposalService.acceptMatchProposal(proposalId, providerId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MatchingErrorCode.SWAP_ACCEPT_NOT_IMPLEMENTED);

        verify(talentRepository, never()).findById(any());
        verify(tradeService, never()).createPurchaseTrade(any());
        verify(creditService, never()).holdForEscrow(any(), anyInt(), any());
        verify(escrowService, never()).create(any(), any(), any(), anyInt());
        verify(chatService, never()).getOrCreateTransactionRoom(any());
        verify(matchProposalRepository, never()).save(any());
    }

    @Test
    @DisplayName("제안을 받은 판매자가 아니면 매칭 제안을 수락할 수 없다")
    void acceptMatchProposal_accessDenied() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;
        Long invalidProviderId = 999L;

        MatchProposal matchProposal = createPurchaseProposal(
                20L,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                100
        );

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        assertThatThrownBy(() -> matchProposalService.acceptMatchProposal(
                proposalId,
                invalidProviderId
        ))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository).findById(proposalId);
        verify(talentRepository, never()).findById(any());
        verify(tradeService, never()).createPurchaseTrade(any());
    }

    @Test
    @DisplayName("이미 수락된 매칭 제안은 기존 수락 결과를 반환한다")
    void acceptMatchProposal_alreadyAccepted() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;
        Long providerTalentId = 20L;

        MatchProposal matchProposal = createPurchaseProposal(
                providerTalentId,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                100
        );

        ReflectionTestUtils.setField(matchProposal, "id", proposalId);
        matchProposal.accept();

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        MatchProposalRes res = matchProposalService.acceptMatchProposal(
                proposalId,
                providerId
        );

        assertThat(res.status()).isEqualTo(MatchProposalStatus.ACCEPTED);
        assertThat(res.respondedAt()).isNotNull();

        verify(matchProposalRepository).findById(proposalId);
        verify(talentRepository, never()).findById(any());
        verify(tradeService, never()).createPurchaseTrade(any());
        verify(creditService, never()).holdForEscrow(any(), anyInt(), any());
        verify(escrowService, never()).create(any(), any(), any(), anyInt());
        verify(chatService, never()).getOrCreateTransactionRoom(any());
        verify(matchProposalRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQUESTED 상태가 아닌 매칭 제안은 수락할 수 없다")
    void acceptMatchProposal_invalidStatus() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;

        MatchProposal matchProposal = createPurchaseProposal(
                20L,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                100
        );
        matchProposal.reject();

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        assertThatThrownBy(() -> matchProposalService.acceptMatchProposal(
                proposalId,
                providerId
        ))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository).findById(proposalId);
        verify(talentRepository, never()).findById(any());
        verify(tradeService, never()).createPurchaseTrade(any());
    }

    @Test
    @DisplayName("판매자는 요청받은 매칭 제안을 거절할 수 있다")
    void rejectMatchProposal() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;

        MatchProposal matchProposal = createPurchaseProposal(
                20L,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                100
        );

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        MatchProposalRes res = matchProposalService.rejectMatchProposal(proposalId, providerId);

        assertThat(res.status()).isEqualTo(MatchProposalStatus.REJECTED);
        assertThat(res.respondedAt()).isNotNull();

        verify(matchProposalRepository).findById(proposalId);
    }

    @Test
    @DisplayName("제안을 받은 판매자가 아니면 매칭 제안을 거절할 수 없다")
    void rejectMatchProposal_accessDenied() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;
        Long invalidProviderId = 999L;

        MatchProposal matchProposal = createPurchaseProposal(
                20L,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                100
        );

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        assertThatThrownBy(() -> matchProposalService.rejectMatchProposal(proposalId, invalidProviderId))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository).findById(proposalId);
    }

    @Test
    @DisplayName("REQUESTED 상태가 아닌 매칭 제안은 거절할 수 없다")
    void rejectMatchProposal_invalidStatus() {
        Long proposalId = 1L;
        Long requesterId = 1L;
        Long providerId = 2L;

        MatchProposal matchProposal = createPurchaseProposal(
                20L,
                requesterId,
                providerId,
                "재능 구매 제안드립니다.",
                100
        );
        matchProposal.accept();

        when(matchProposalRepository.findById(proposalId))
                .thenReturn(Optional.of(matchProposal));

        assertThatThrownBy(() -> matchProposalService.rejectMatchProposal(proposalId, providerId))
                .isInstanceOf(CustomException.class);

        verify(matchProposalRepository).findById(proposalId);
    }

    private MatchProposal createPurchaseProposal(
            Long providerTalentId,
            Long requesterId,
            Long providerId,
            String requestMessage,
            Integer providerTalentPriceSnapshot
    ) {
        return MatchProposal.create(
                providerTalentId,
                null,
                requesterId,
                providerId,
                requestMessage,
                providerTalentPriceSnapshot,
                null,
                null
        );
    }

    private Talent createTalent(Long id, Long authorId) {
        Category category = createCategory();

        Talent talent = Talent.create(
                authorId,
                category,
                "테스트 재능",
                "테스트 내용",
                2,
                100
        );

        ReflectionTestUtils.setField(talent, "id", id);

        return talent;
    }

    private Category createCategory() {
        try {
            Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Category category = constructor.newInstance();

            ReflectionTestUtils.setField(category, "id", 1L);
            ReflectionTestUtils.setField(category, "name", "백엔드");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);

            return category;
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }
}