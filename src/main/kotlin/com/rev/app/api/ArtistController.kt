package com.rev.app.api

import com.rev.app.services.ArtistService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artists")
class ArtistController(private val svc: ArtistService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "NAME") sort: String,
        @RequestParam(defaultValue = "40") size: Int,
        @RequestParam(defaultValue = "0") page: Int
    ): PageResp<ArtistItem> {
        val items = svc.list(q, if (sort == "POPULAR") "POPULAR" else "NAME", size, page)
        return PageResp(items, null)
    }
}
