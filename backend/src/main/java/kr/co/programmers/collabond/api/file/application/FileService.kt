package kr.co.programmers.collabond.api.file.application

import kr.co.programmers.collabond.api.file.domain.File
import kr.co.programmers.collabond.api.file.infrastructure.FileRepository
import kr.co.programmers.collabond.api.file.interfaces.FileMapper
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.InvalidException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

@Service
class FileService(
    private val fileRepository: FileRepository
) {

    @Value("\${custom.file.path}")
    private val fileDir: String? = null

    @Transactional
    fun saveFile(multipartFile: MultipartFile): File {
        val originFileName: String? = multipartFile.originalFilename
        val savedFileName = createStoreFileName(originFileName!!)
        try {
            multipartFile.transferTo(java.io.File(getFullPath(savedFileName)))
        } catch (e: IOException) {
            throw InvalidException(ErrorCode.INVALID_REQUEST)
        }

        val file: File = FileMapper.toEntity(originFileName, savedFileName)

        return fileRepository.save(file)
    }

    fun getFullPath(fileName: String): String {
        return fileDir + fileName
    }

    private fun createStoreFileName(originalFilename: String): String {
        val ext = extractExt(originalFilename)
        val uuid: String = UUID.randomUUID().toString()
        return "$uuid.$ext"
    }

    private fun extractExt(originalFilename: String): String {
        val pos = originalFilename.lastIndexOf(".")
        return originalFilename.substring(pos + 1)
    }

    @Transactional
    fun saveFiles(files: List<MultipartFile>): MutableList<File> {
        val savedFilesResult = ArrayList<File>()
        for (file in files) {
            if (!file.isEmpty) {
                savedFilesResult.add(saveFile(file))
            }
        }
        return savedFilesResult
    }
}
