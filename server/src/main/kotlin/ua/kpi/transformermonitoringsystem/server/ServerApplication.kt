package ua.kpi.transformermonitoringsystem.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["ua.kpi.transformermonitoringsystem"])
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
