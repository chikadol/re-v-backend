import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class DemoController {
    @GetMapping("/api/me")
    fun me(auth: org.springframework.security.core.Authentication) =
        mapOf("user" to auth.name, "roles" to auth.authorities.map { it.authority })
}
