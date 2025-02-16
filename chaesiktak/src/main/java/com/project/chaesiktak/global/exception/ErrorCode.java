package com.project.chaesiktak.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    // 400 Bad Request
    VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    PASSWORD_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST,"비밀번호를 확인해주세요."),
    ALREADY_EXIT_EMAIL_EXCEPTION(HttpStatus.BAD_REQUEST,"이미 존재하는 이메일입니다."),
    ALREADY_EXIT_NICKNAME_EXCEPTION(HttpStatus.BAD_REQUEST,"이미 존재하는 닉네임입니다."),
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "필수적인 요청 값이 입력되지 않았습니다."),
    VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 헤더값이 입력되지 않았습니다."),
    VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 파라미터값이 입력되지 않았습니다."),
    REQUEST_METHOD_VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 메소드가 잘못됐습니다."),
    VALIDATION_REQUEST_FAIL_USERINFO_EXCEPTION(HttpStatus.BAD_REQUEST,"사용자 정보를 받아오는데 실패했습니다."),
    VALIDATION_JSON_SYNTAX_FAIL(HttpStatus.BAD_REQUEST, "JSON 파싱 오류 발생"),
    INVALID_ROLE_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "올바르지 않은 권한 요청입니다."),
    INVALID_ID_EXCEPTION(HttpStatus.BAD_REQUEST, "사용자 ID가 유효하지 않습니다. "),
    INVALID_SIGNATURE_EXCEPTION(HttpStatus.BAD_REQUEST, "JWT 토큰의 서명이 올바르지 않습니다."),
    INVALID_DISPLAY_NAME_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 displayName이 있습니다 "),
    NUMBER_LESS_THAN_ZERO_EXCEPTION(HttpStatus.BAD_REQUEST, "페이지의 크기 번호나 페이지의 사이즈는 0 미만일 수 없습니다. "),
    SMS_CERTIFICATION_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    SMS_CERTIFICATION_SEND_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "인증번호 발송에 실패했습니다."),
    SMS_CERTIFICATION_VERIFY_FAIL_EXCEPTION(HttpStatus.BAD_REQUEST, "문자 인증에 실패했습니다."),
    EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "이메일 인증 발송에 실패했습니다."),
    FAIL_ENCODING_IMAGE_FILE_NAME(HttpStatus.BAD_REQUEST, "파일명 인코딩에 실패했습니다."),
    INVALID_FILE_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일입니다."),

    // 401 Unauthorized
    UNAUTHORIZED_EMAIL_EXCEPTION(HttpStatus.UNAUTHORIZED, "이메일 인증이 필요합니다."),
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자거나 인증과정에 오류가 있습니다. "),
    INVALID_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ONLY_OWN_EVENT_MODIFY_EXCEPTION(HttpStatus.UNAUTHORIZED, "본인이 작성한 Event만 수정/삭제 가능합니다."),

    // 403 Forbidden
    FORBIDDEN_AUTH_EXCEPTION(HttpStatus.FORBIDDEN, "권한 정보가 없는 토큰입니다."),
    EXPIRED_TOKEN_EXCEPTION(HttpStatus.FORBIDDEN, "토큰이 만료되었습니다."),
    ACCESS_DENIED_EXCEPTION(HttpStatus.FORBIDDEN, "접근 권한이 없습니다. "),
    AUTHENTICATION_FAILED_EXCEPTION(HttpStatus.FORBIDDEN, "인증에 실패했습니다. "),

    // 404 NOT FOUND
    NOT_FOUND_ID_EXCEPTION(HttpStatus.NOT_FOUND, "찾을 수 없는 ID"),
    NOT_FOUND_CATEGORY_IN_INTEREST_EXCEPTION(HttpStatus.NOT_FOUND, "해당 관심사를 찾을 수 없습니다."),
    NOT_FOUND_INTEREST_CATEGORY_IN_EVENT(HttpStatus.NOT_FOUND, "등록된 일정이 없습니다."),
    NOT_FOUND_EVENT_DATA_EXCEPTION(HttpStatus.NOT_FOUND, "해당 검색 필터에 부합하는 이벤트 목록이 없습니다."),
    NOT_FOUND_TODAY_EVENT_EXCEPTION(HttpStatus.NOT_FOUND, "조회에 성공했으나 오늘 하루 일정이 없습니다."),
    NOT_FOUND_USER_EXCEPTION(HttpStatus.NOT_FOUND, "해당 검색결과에 해당하는 사용자를 찾을 수 없습니다."),
    NOT_FOUND_STUDENT_NUM_EXCEPTION(HttpStatus.NOT_FOUND, "해당 학번에 해당하는 학생을 찾을 수 없습니다"),

    // 409 Conflict
    ALREADY_EXIST_STUDENT_EXCEPTION(HttpStatus.CONFLICT, "이미 회원가입이 완료된 사용자입니다."),
    ALREADY_EXIST_SUBJECT_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 제목입니다."),

    // 500 Internal Server Exception
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다."),
    TOKEN_CREATION_FAILED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "토큰을 생성하는 과정에서 알 수 없는 오류가 발생했습니다."),

    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "입출력 오류가 발생했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다."),

    // 503 Service Unavailable
    FAILED_GET_TOKEN_EXCEPTION(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, "구글 엑세스 토큰을 가져오는데 실패했습니다."),
    FAILED_UPLOAD_IMAGE_FILE_EXCEPTION(HttpStatus.SERVICE_UNAVAILABLE, "이미지를 업로드하는데 실패했습니다. "),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // Recipe
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode(){
        return httpStatus.value();
    }
}
