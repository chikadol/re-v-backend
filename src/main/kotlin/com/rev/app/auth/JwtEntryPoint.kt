import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.naming.AuthenticationException

@Component
abstract class JwtEntryPoint: AuthenticationEntryPoint {
    fun commence(req: HttpServletRequest, res: HttpServletResponse, ex: AuthenticationException) {
        res.status = HttpServletResponse.SC_UNAUTHORIZED
        res.contentType = "application/json"
        res.writer.write("""{"error":"unauthorized"}""")
    }
}
