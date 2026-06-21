package com.back.baton.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${cookie.secure:true}")
    private boolean isSecure;

    public void setCookie(HttpServletResponse response, String name, String value, Long maxAge){
        // 로그인 유지 별도 구현하는 경우 maxAge 설정

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Strict") ;// CSRF 방지 (Lax, Strict, None)

        if(maxAge!=null){
            cookieBuilder.maxAge(maxAge);
        }
        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
