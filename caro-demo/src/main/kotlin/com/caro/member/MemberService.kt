package com.caro.member

/**
 * Member 모듈의 외부 공개 인터페이스.
 */
interface MemberService {

    fun getMember(memberId: Long): MemberDto?

    fun getMemberByEmail(email: String): MemberDto?
    
    fun createMember(email: String, nickname: String): MemberDto
    
    fun updateNickname(memberId: Long, nickname: String): MemberDto
}

