package ua.kpi.transformermonitoringsystem.data

data class ModeOption(
    val title: String,
    val modeName: String,
    val temp: Int,
    val oil: String,
    val warning: String,
    val isDanger: Boolean = false
)
