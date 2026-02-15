package com.caro.member.internal

import com.caro.member.MemberDto
import com.caro.member.MemberService
import com.caro.member.exception.MemberException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class MemberServiceImpl(
    private val memberRepository: MemberRepository
) : MemberService {

    @Transactional(readOnly = true)
    override fun getMember(memberId: Long): MemberDto? {
        return memberRepository.findById(memberId)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun getMemberByEmail(email: String): MemberDto? {
        return memberRepository.findByEmail(email)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional
    override fun createMember(email: String, nickname: String): MemberDto {
        if (memberRepository.existsByEmail(email)) {
            throw MemberException.EmailAlreadyExistsException(email)
        }
        val member = Member(email = email, nickname = nickname)
        return memberRepository.save(member).toDto()
    }

    @Transactional
    override fun updateNickname(memberId: Long, nickname: String): MemberDto {
        val member = memberRepository.findById(memberId)
            .orElseThrow { MemberException.MemberNotFoundException(memberId) }
        member.nickname = nickname
        return memberRepository.save(member).toDto()
    }

    private fun Member.toDto() = MemberDto(
        id = id,
        email = email,
        nickname = nickname,
        createdAt = createdAt
    )
}
