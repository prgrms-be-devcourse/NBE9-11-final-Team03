package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.AttachmentSaveReq;
import com.back.baton.domain.talent.dto.request.PresignedUrlReq;
import com.back.baton.domain.talent.dto.response.AttachmentRes;
import com.back.baton.domain.talent.dto.response.PresignedUrlRes;
import com.back.baton.domain.talent.service.TalentAttachmentService;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TalentAttachmentController.class)
class TalentAttachmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TalentAttachmentService talentAttachmentService;

    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String BASE = "/api/v1/talents/1/attachments";

    // ===== presigned URL 발급 =====

    @Test
    @DisplayName("presigned URL 발급 성공 - 200, uploadUrl/key 반환")
    void createPresignedUrl_success() throws Exception {
        var res = new PresignedUrlRes("https://bucket.s3.../talents/1/uuid-photo.png", "talents/1/uuid-photo.png");
        given(talentAttachmentService.createPresignedUrl(eq(1L), eq(7L), any(PresignedUrlReq.class)))
                .willReturn(res);

        var req = new PresignedUrlReq("photo.png", "image/png");

        mockMvc.perform(post(BASE + "/presigned-url")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-3"))
                .andExpect(jsonPath("$.data.uploadUrl").value(res.uploadUrl()))
                .andExpect(jsonPath("$.data.key").value(res.key()));
    }

    @Test
    @DisplayName("contentType이 image/* 패턴이 아니면 400")
    void createPresignedUrl_invalidContentType() throws Exception {
        var req = new PresignedUrlReq("malware.exe", "application/x-msdownload");

        mockMvc.perform(post(BASE + "/presigned-url")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }

    @Test
    @DisplayName("fileName이 비어있으면 400")
    void createPresignedUrl_blankFileName() throws Exception {
        var req = new PresignedUrlReq("", "image/png");

        mockMvc.perform(post(BASE + "/presigned-url")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }

    @Test
    @DisplayName("남의 재능에 발급 시도하면 403")
    void createPresignedUrl_forbidden() throws Exception {
        willThrow(new CustomException(TalentErrorCode.ATTACHMENT_FORBIDDEN))
                .given(talentAttachmentService).createPresignedUrl(eq(1L), eq(2L), any());

        var req = new PresignedUrlReq("photo.png", "image/png");

        mockMvc.perform(post(BASE + "/presigned-url")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TALENT-403-002"));
    }

    // ===== 첨부 저장 =====

    @Test
    @DisplayName("첨부 저장 성공 - 201")
    void saveAttachment_success() throws Exception {
        var res = new AttachmentRes(10L, 1L, "talents/1/uuid-photo.png", "샘플", LocalDateTime.now());
        given(talentAttachmentService.saveAttachment(eq(1L), eq(7L), any(AttachmentSaveReq.class)))
                .willReturn(res);

        var req = new AttachmentSaveReq("talents/1/uuid-photo.png", "샘플");

        mockMvc.perform(post(BASE)
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-4"))
                .andExpect(jsonPath("$.data.attachmentId").value(10L))
                .andExpect(jsonPath("$.data.url").value(res.url()));
    }

    @Test
    @DisplayName("url이 비어있으면 400")
    void saveAttachment_blankUrl() throws Exception {
        var req = new AttachmentSaveReq("", "샘플");

        mockMvc.perform(post(BASE)
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }

    // ===== 목록 조회 (공개) =====

    @Test
    @DisplayName("첨부 목록 조회 성공 - 200, 인증 헤더 불필요")
    void getAttachments_success() throws Exception {
        given(talentAttachmentService.getAttachments(1L)).willReturn(List.of(
                new AttachmentRes(10L, 1L, "url1", "첫번째", LocalDateTime.now()),
                new AttachmentRes(11L, 1L, "url2", "두번째", LocalDateTime.now())
        ));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-3"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].url").value("url1"));
    }

    @Test
    @DisplayName("없는 재능 첨부 목록 조회는 404")
    void getAttachments_talentNotFound() throws Exception {
        given(talentAttachmentService.getAttachments(99L))
                .willThrow(new CustomException(TalentErrorCode.TALENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/talents/99/attachments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TALENT-404-001"));
    }

    // ===== 삭제 =====

    @Test
    @DisplayName("첨부 삭제 성공 - 200")
    void deleteAttachment_success() throws Exception {
        doNothing().when(talentAttachmentService).deleteAttachment(1L, 10L, 7L);

        mockMvc.perform(delete(BASE + "/10")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-3"))
                .andExpect(jsonPath("$.data").isEmpty());

        then(talentAttachmentService).should().deleteAttachment(1L, 10L, 7L);
    }

    @Test
    @DisplayName("남의 첨부 삭제 시도하면 403")
    void deleteAttachment_forbidden() throws Exception {
        willThrow(new CustomException(TalentErrorCode.ATTACHMENT_FORBIDDEN))
                .given(talentAttachmentService).deleteAttachment(1L, 10L, 2L);

        mockMvc.perform(delete(BASE + "/10")
                        .header("X-User-Id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TALENT-403-002"));
    }
}