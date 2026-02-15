package com.caro.shared

import java.time.Instant

/**
 * 공통 API 응답 래퍼.
 * 모든 REST API 응답에 사용되는 표준 형식.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
        
        fun <T> error(code: String, message: String): ApiResponse<T> = ApiResponse(
            success = false,
            error = ErrorDetail(code, message)
        )
    }
}

data class ErrorDetail(
    val code: String,
    val message: String
)
