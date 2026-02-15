package com.caro.member.internal.web

import com.caro.member.MemberDto
import com.caro.member.MemberService
import com.caro.shared.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMember(@Valid @RequestBody request: CreateMemberRequest): ApiResponse<MemberDto> {
        val member = memberService.createMember(request.email, request.nickname)
        return ApiResponse.ok(member)
    }

    @GetMapping("/{memberId}")
    fun getMember(@PathVariable memberId: Long): ApiResponse<MemberDto> {
        val member = memberService.getMember(memberId)
            ?: return ApiResponse.error("NOT_FOUND", "Member not found: $memberId")
        return ApiResponse.ok(member)
    }

    @PatchMapping("/{memberId}/nickname")
    fun updateNickname(
        @PathVariable memberId: Long,
        @Valid @RequestBody request: UpdateNicknameRequest
    ): ApiResponse<MemberDto> {
        val member = memberService.updateNickname(memberId, request.nickname)
        return ApiResponse.ok(member)
    }
}


