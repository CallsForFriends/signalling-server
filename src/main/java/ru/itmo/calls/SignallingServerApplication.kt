package ru.itmo.calls

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class SignallingServerApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SignallingServerApplication>(*args)
        }
    }
}