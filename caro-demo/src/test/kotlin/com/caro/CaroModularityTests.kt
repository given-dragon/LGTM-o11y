package com.caro

import io.kotest.core.spec.style.DescribeSpec
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class CaroModularityTests : DescribeSpec({
    val modules = ApplicationModules.of(CaroApplication::class.java)

    describe("모듈 아키텍처 검증") {
        it("Spring Modulith 규칙을 준수해야 한다") {
            try {
                modules.verify()
            } catch (e: Exception) {
                println("====== MODULITH VIOLATION REPORT START ======")
                println(e.message)
                println("====== MODULITH VIOLATION REPORT END ======")
                throw e
            }
        }

        it("모듈 구조 문서를 생성한다 (PlantUML)") {
            Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
        }
    }
})
