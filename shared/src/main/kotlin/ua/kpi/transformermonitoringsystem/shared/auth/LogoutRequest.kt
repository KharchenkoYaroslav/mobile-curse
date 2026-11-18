package ua.kpi.transformermonitoringsystem.shared.auth

interface LogoutRequest {
    val refreshToken: String?
}
