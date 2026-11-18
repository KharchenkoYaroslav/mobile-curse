package ua.kpi.transformermonitoringsystem.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ua.kpi.transformermonitoringsystem.auth.jwt.JwtService
import ua.kpi.transformermonitoringsystem.user.repository.UserRepository
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.endsWith("/auth/register") || uri.endsWith("/auth/register/") ||
               uri.endsWith("/auth/login") || uri.endsWith("/auth/login/") ||
               uri.endsWith("/auth/refresh") || uri.endsWith("/auth/refresh/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authHeader.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val parts = authHeader.trim().split(" ")
        if (parts.size != 2 || parts[0] != "Bearer" || parts[1].isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Невірний тип авторизації")
            return
        }

        val token = parts[1]
        try {
            val userId = jwtService.extractUserIdFromAccessToken(token)
            val authorities = try {
                val user = userRepository.findById(UUID.fromString(userId)).orElse(null)
                if (user != null) {
                    listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                emptyList()
            }

            val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
            SecurityContextHolder.getContext().authentication = auth
            filterChain.doFilter(request, response)
        } catch (_: Exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Не валідний токен авторизації")
        }
    }
}
