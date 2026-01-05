package com.rev.app.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "REV API",
        version = "v1",
        description = """
            REV 백엔드 API 문서
            
            ## 인증
            대부분의 API는 JWT 토큰 인증이 필요합니다. 
            `/auth/login` 또는 `/auth/register` 엔드포인트를 통해 토큰을 발급받아 사용하세요.
            
            ## 응답 형식
            모든 API는 통일된 응답 형식을 사용합니다:
            - `success`: 요청 성공 여부
            - `data`: 응답 데이터
            - `message`: 성공/실패 메시지
            - `error`: 에러 정보 (실패 시)
            
            ## 페이징
            목록 조회 API는 페이징을 지원합니다:
            - `page`: 페이지 번호 (0부터 시작)
            - `size`: 페이지 크기
            - `sort`: 정렬 기준 (예: `createdAt,desc`)
        """.trimIndent()
    ),
    security = [
        SecurityRequirement(name = "bearerAuth") // 🔐 기본 보안 스키마
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT 토큰을 사용한 인증. Bearer {token} 형식으로 전송하세요."
)
class OpenApiConfig
