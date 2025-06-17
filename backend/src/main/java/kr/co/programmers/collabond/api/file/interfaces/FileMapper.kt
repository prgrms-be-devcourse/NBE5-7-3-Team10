package kr.co.programmers.collabond.api.file.interfaces

import kr.co.programmers.collabond.api.file.domain.File

object FileMapper {
    fun toEntity(
        originFileName: String,
        savedFileName: String
    ): File = File(
        originName = originFileName,
        savedName = savedFileName
    )
}
