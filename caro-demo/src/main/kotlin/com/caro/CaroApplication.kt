package com.caro

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulithic
import org.springframework.scheduling.annotation.EnableAsync

@Modulithic(
    systemName = "Caro",
    sharedModules = ["shared"]
)
@EnableAsync
@SpringBootApplication
class CaroApplication

fun main(args: Array<String>) {
    runApplication<CaroApplication>(*args)
}
