package com.caro.shared.exception

/**
 * 비즈니스 로직 예외 베이스 클래스.
 */
sealed class BusinessException(
    val code: String,
    override val message: String
) : RuntimeException(message)

class NotFoundException(
    entityName: String,
    id: Any
) : BusinessException("NOT_FOUND", "$entityName not found: $id")

class DuplicateException(
    entityName: String,
    field: String,
    value: Any
) : BusinessException("DUPLICATE", "$entityName with $field '$value' already exists")

class InvalidRequestException(
    reason: String
) : BusinessException("INVALID_REQUEST", reason)
