package ua.kpi.transformermonitoringsystem.shared.core

interface ITransformer {
    val id: Int
    val name: String
    val location: String
    val currentLoad: Double
    val temperature: Double
    val maxLoad: Double
}

interface ITransformerStatus {
    val id: Int
    val transformerId: Int
    val voltage: Double
    val temperature: Double
    val load: Double
}

interface ITransformerStats {
    val totalPower: Double
    val criticalIssues: Int
    val averageLoad: Double
    val averageTemperature: Double
    val totalCount: Int
}
