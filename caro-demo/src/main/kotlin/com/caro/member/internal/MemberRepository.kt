package com.caro.member.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

internal interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Optional<Member>
    fun existsByEmail(email: String): Boolean
}
