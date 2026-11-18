package ua.kpi.transformermonitoringsystem.shared.auth

interface AuthResponse {
    val accessToken: String
    val refreshToken: String
}
