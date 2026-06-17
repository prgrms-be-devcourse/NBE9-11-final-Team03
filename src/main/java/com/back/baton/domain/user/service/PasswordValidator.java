package com.back.baton.domain.user.service;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {
//      [비밀번호 규칙 검증 로직]
//      비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자
//      허용 특수문자: ! @ # $ % ^ * ( ) _ + ~ 등 키보드로 입력 가능한 특수문자 포함.
//      연속성 금지: 동일한 문자/숫자 3자 이상 연속 사용 금지 (ex. aaa, 111)
//      아이디 포함 금지: 이메일에서 도메인 앞부분 포함 금지 (ex. 이메일: user12@gmail.com라면 비밀번호에 user12 포함 금지)

    private static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^*()_+~]).{8,20}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public boolean validate(String password, String username){

        // 비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자, 허용 특수문자 검증
        if(!PASSWORD_PATTERN.matcher(password).matches()){
            return false;
        }

        // 동일 문자 3회 연속 반복 검증 (ex. aaa, 111)
        if(hasRepeatedChars(password)){
            return false;
        }

        // 아이디 포함 여부 검증
        if(username!=null && !username.isBlank() && password.contains(username)) {
            return false;
        }

        return true;
    }
    private static boolean hasRepeatedChars(String password){
        for(int i=0; i<password.length()-2; i++){
            if(password.charAt(i) == password.charAt(i+1) && password.charAt(i+1) == password.charAt(i+2)){
                return true;
            }
        }
        return false;
    }

}
