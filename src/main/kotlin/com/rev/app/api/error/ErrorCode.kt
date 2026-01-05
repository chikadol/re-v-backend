package com.rev.app.api.error

import org.springframework.http.HttpStatus

/**
 * API 에러 코드 체계
 * 
 * 형식: {도메인}_{타입}_{상세}
 * 예: THREAD_NOT_FOUND, AUTH_TOKEN_EXPIRED, VALIDATION_FIELD_REQUIRED
 */
enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    // ========== 공통 에러 (COMMON) ==========
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FORBIDDEN("FORBIDDEN", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    
    // ========== 검증 에러 (VALIDATION) ==========
    VALIDATION_ERROR("VALIDATION_ERROR", "입력값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_FIELD_REQUIRED("VALIDATION_FIELD_REQUIRED", "필수 필드가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_FIELD_INVALID("VALIDATION_FIELD_INVALID", "필드 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_EMAIL_INVALID("VALIDATION_EMAIL_INVALID", "올바른 이메일 형식이 아닙니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_SIZE_INVALID("VALIDATION_SIZE_INVALID", "필드 길이가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    
    // ========== 인증/인가 에러 (AUTH) ==========
    AUTH_LOGIN_FAILED("AUTH_LOGIN_FAILED", "로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID("AUTH_TOKEN_INVALID", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_MISSING("AUTH_TOKEN_MISSING", "토큰이 제공되지 않았습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_PASSWORD_INCORRECT("AUTH_PASSWORD_INCORRECT", "비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_USER_NOT_FOUND("AUTH_USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    AUTH_USER_ALREADY_EXISTS("AUTH_USER_ALREADY_EXISTS", "이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    
    // ========== 게시판 에러 (BOARD) ==========
    BOARD_NOT_FOUND("BOARD_NOT_FOUND", "게시판을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOARD_SLUG_DUPLICATE("BOARD_SLUG_DUPLICATE", "이미 존재하는 슬러그입니다.", HttpStatus.CONFLICT),
    BOARD_CREATE_FAILED("BOARD_CREATE_FAILED", "게시판 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BOARD_DELETE_FAILED("BOARD_DELETE_FAILED", "게시판 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ========== 게시글 에러 (THREAD) ==========
    THREAD_NOT_FOUND("THREAD_NOT_FOUND", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    THREAD_CREATE_FAILED("THREAD_CREATE_FAILED", "게시글 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    THREAD_UPDATE_FAILED("THREAD_UPDATE_FAILED", "게시글 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    THREAD_DELETE_FAILED("THREAD_DELETE_FAILED", "게시글 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    THREAD_LIST_FAILED("THREAD_LIST_FAILED", "게시글 목록을 불러오는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    THREAD_DETAIL_FAILED("THREAD_DETAIL_FAILED", "게시글을 불러오는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    THREAD_ACCESS_DENIED("THREAD_ACCESS_DENIED", "이 게시글에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    
    // ========== 댓글 에러 (COMMENT) ==========
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMMENT_CREATE_FAILED("COMMENT_CREATE_FAILED", "댓글 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMENT_DELETE_FAILED("COMMENT_DELETE_FAILED", "댓글 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMENT_ACCESS_DENIED("COMMENT_ACCESS_DENIED", "이 댓글에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    
    // ========== 알림 에러 (NOTIFICATION) ==========
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOTIFICATION_LIST_FAILED("NOTIFICATION_LIST_FAILED", "알림 목록을 불러오는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ========== 북마크 에러 (BOOKMARK) ==========
    BOOKMARK_ALREADY_EXISTS("BOOKMARK_ALREADY_EXISTS", "이미 북마크된 게시글입니다.", HttpStatus.CONFLICT),
    BOOKMARK_NOT_FOUND("BOOKMARK_NOT_FOUND", "북마크를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOKMARK_TOGGLE_FAILED("BOOKMARK_TOGGLE_FAILED", "북마크 토글에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ========== 반응 에러 (REACTION) ==========
    REACTION_INVALID_TYPE("REACTION_INVALID_TYPE", "유효하지 않은 반응 타입입니다.", HttpStatus.BAD_REQUEST),
    REACTION_TOGGLE_FAILED("REACTION_TOGGLE_FAILED", "반응 토글에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ========== 사용자 관리 에러 (USER) ==========
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_UPDATE_FAILED("USER_UPDATE_FAILED", "사용자 정보 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_DELETE_FAILED("USER_DELETE_FAILED", "사용자 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ROLE_UPDATE_FAILED("USER_ROLE_UPDATE_FAILED", "사용자 역할 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    
    companion object {
        /**
         * 코드로 ErrorCode 찾기
         */
        fun fromCode(code: String): ErrorCode? {
            return values().find { it.code == code }
        }
    }
}

