package ua.kpi.transformermonitoringsystem.core.service

import org.springframework.stereotype.Service
import ua.kpi.transformermonitoringsystem.shared.ITransformerMonitorService

@Service
class TransformerMonitorServiceImpl : ITransformerMonitorService {
    override fun getHealthStatus(): String {
        return "All systems operational"
    }
}
