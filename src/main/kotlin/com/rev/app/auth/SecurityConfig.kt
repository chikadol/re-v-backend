import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsConfigurationSource

@Bean
fun corsConfigurationSource(): CorsConfigurationSource {
    val c = CorsConfiguration()
    c.allowedOrigins = listOf(
        // 프런트 도메인 확정되면 교체 (예: http://localhost:5173, https://app.rev.com 등)
        "http://localhost:3000",
        "http://localhost:5173"
    )
    c.allowedMethods = listOf("GET","POST","PUT","PATCH","DELETE","OPTIONS")
    c.allowedHeaders = listOf("*")
    c.exposedHeaders = listOf("Authorization")
    c.allowCredentials = true

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", c)
    return source
}
