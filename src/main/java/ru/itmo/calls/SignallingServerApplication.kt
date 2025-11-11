package ru.itmo.calls

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SignallingServerApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SignallingServerApplication>(*args)
        }
    }
}