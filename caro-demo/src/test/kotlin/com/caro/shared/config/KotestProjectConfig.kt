package com.caro.shared.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class KotestProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(SpringExtension())
}
