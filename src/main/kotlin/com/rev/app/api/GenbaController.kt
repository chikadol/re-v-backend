package com.rev.app.api

import com.rev.app.services.GenbaService
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/genbas")
class GenbaController(private val svc: GenbaService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) date_from: Instant?,
        @RequestParam(required = false) date_to: Instant?,
        @RequestParam(required = false) area: String?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) cursor_at: Instant?,
        @RequestParam(required = false) cursor_id: Long?,
        @RequestParam(defaultValue = "RECENT") sort: String,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "0") page: Int
    ): PageResp<GenbaItem> {
        val items = svc.list(date_from, date_to, area, q, cursor_at, cursor_id, if (sort == "POPULAR") "POPULAR" else "RECENT", size, page)
        return PageResp(items, null)
    }
}
