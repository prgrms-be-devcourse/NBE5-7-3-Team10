package kr.co.programmers.collabond.api.file.interfaces

import kr.co.programmers.collabond.api.file.application.FileService
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.MalformedURLException

@RestController
@RequestMapping("/api/files")
class FileController(
    private val fileService: FileService
) {

    @GetMapping("/images/{filename}")
    @Throws(MalformedURLException::class)
    fun showImage(
        @PathVariable("filename") filename: String
    ): Resource {
        return UrlResource("file:" + fileService.getFullPath(filename))
    }
}
