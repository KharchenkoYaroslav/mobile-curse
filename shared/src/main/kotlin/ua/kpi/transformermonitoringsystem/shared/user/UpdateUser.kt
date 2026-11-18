package ua.kpi.transformermonitoringsystem.shared.user

interface UpdateUser {
    val currentPassword: String?
    val login: String?
    val password: String?
}
