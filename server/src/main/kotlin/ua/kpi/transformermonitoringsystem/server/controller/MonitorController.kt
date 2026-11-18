package ua.kpi.transformermonitoringsystem.server.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ua.kpi.transformermonitoringsystem.shared.ITransformerMonitorService

@RestController
@RequestMapping
class MonitorController(
    private val monitorService: ITransformerMonitorService
) {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "system" to monitorService.getHealthStatus()
        )
    }
}
