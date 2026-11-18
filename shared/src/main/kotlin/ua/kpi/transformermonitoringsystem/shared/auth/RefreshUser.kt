package ua.kpi.transformermonitoringsystem.shared.auth

interface RefreshUser {
    val sub: String
    val refreshToken: String?
}
