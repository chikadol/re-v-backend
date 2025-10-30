package com.rev.app

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RootController {
    @GetMapping("/")
    fun index(): String = "redirect:/swagger-ui/index.html"
}