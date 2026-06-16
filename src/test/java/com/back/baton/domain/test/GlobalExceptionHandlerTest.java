package com.back.baton.domain.test;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.exception.GlobalExceptionHandler;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.code.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("성공 응답은 ApiResponse 형식으로 반환된다")
    public void successResponse() throws Exception {
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-0"))
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    @DisplayName("CustomException 발생 시 ErrorCode 기준의 실패 응답이 반환된다")
    public void customException() throws Exception {
        mockMvc.perform(get("/test/custom-exception"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("@Valid 검증 실패 시 필드별 에러 메시지가 반환된다")
    public void validationException() throws Exception {
        String requestBody = """
                {
                  "email": "wrong-email",
                  "password": "1234"
                }
                """;

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("400-2"))
                .andExpect(jsonPath("$.message").value("요청 필드 검증에 실패했습니다."))
                .andExpect(jsonPath("$.data.email").value("이메일 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.password").value("비밀번호는 8자 이상이어야 합니다."));
    }

    @Test
    @DisplayName("예상하지 못한 예외 발생 시 500 공통 응답이 반환된다")
    public void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("500-1"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @RestController
    public static class TestController {

        @GetMapping("/test/success")
        public ApiResponse<String> success() {
            return ApiResponse.success("success");
        }

        @GetMapping("/test/custom-exception")
        public ApiResponse<Void> customException() {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        @PostMapping("/test/validation")
        public ApiResponse<String> validation(@Valid @RequestBody TestRequest request) {
            return ApiResponse.success("validation success");
        }

        @GetMapping("/test/server-error")
        public ApiResponse<Void> serverError() {
            throw new RuntimeException("unexpected error");
        }
    }

    public record TestRequest(

            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
            String password
    ) {
    }
}