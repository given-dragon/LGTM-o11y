package com.caro.member.exception

sealed class MemberException(message: String) : RuntimeException(message) {
    class MemberNotFoundException(memberId: Long) : MemberException("Member not found: $memberId")
    class EmailAlreadyExistsException(email: String) : MemberException("Email already exists: $email")
}
