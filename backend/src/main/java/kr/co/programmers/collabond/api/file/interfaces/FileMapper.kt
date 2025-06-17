package kr.co.programmers.collabond.api.file.interfaces

import kr.co.programmers.collabond.api.file.domain.File

object FileMapper {
    @JvmStatic
    fun toEntity(
        originFileName: String,
        savedFileName: String
    ): File = File(
        originName = originFileName,
        savedName = savedFileName
    )
}
