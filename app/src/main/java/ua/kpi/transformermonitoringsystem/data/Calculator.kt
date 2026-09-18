package ua.kpi.transformermonitoringsystem.data

private const val SQRT_3 = 1.73205081
class Calculator {
    fun calculatePowerAdvanced(voltage: String, current: String, cosPhi: String): Double {
        val u = voltage.toDoubleOrNull() ?: 0.0
        val i = current.toDoubleOrNull() ?: 0.0
        val cos = cosPhi.toDoubleOrNull() ?: 0.0

        return SQRT_3 * u * i * cos
    }
}
