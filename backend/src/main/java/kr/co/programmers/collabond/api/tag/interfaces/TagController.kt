package kr.co.programmers.collabond.api.tag.interfaces

import kr.co.programmers.collabond.api.tag.application.TagService
import kr.co.programmers.collabond.api.tag.domain.dto.Requests
import kr.co.programmers.collabond.api.tag.domain.dto.Responses
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequiredArgsConstructor
class TagController {
    private val tagService: TagService? = null

    @get:GetMapping("/api/tags")
    val allTags: ResponseEntity<List<Responses>>
        // 전체 태그 목록 조회
        get() {
            val tags = tagService!!.findAll()
            return ResponseEntity.ok(tags)
        }

    // 태그 추가
    @PostMapping("/admin/tags")
    fun createTag(@RequestBody request: Requests): ResponseEntity<Responses> {
        val response = tagService!!.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // 태그 삭제
    @DeleteMapping("/admin/tags/{tagId}")
    fun deleteTag(@PathVariable tagId: Long): ResponseEntity<Void> {
        tagService!!.delete(tagId)
        return ResponseEntity.noContent().build()
    }
}
