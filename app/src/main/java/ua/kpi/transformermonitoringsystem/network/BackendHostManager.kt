package ua.kpi.transformermonitoringsystem.network

import ua.kpi.transformermonitoringsystem.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

// Логіка побудови посилань та налаштування хостів
@Singleton
class BackendHostManager @Inject constructor() {
    private val candidateHosts = BuildConfig.BACKEND_HOSTS.split(",")
    var activeBackendHost: String = candidateHosts.firstOrNull() ?: "10.0.2.2"
}
