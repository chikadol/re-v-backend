package com.rev.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RevApplication

fun main(args: Array<String>) {
    runApplication<RevApplication>(*args)
}
