package kr.co.programmers.collabond.api.file.infrastructure

import kr.co.programmers.collabond.api.file.domain.File
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, Long>
