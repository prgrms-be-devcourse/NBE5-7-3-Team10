package kr.co.programmers.collabond.api.file.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.co.programmers.collabond.shared.domain.OnlyCreatedEntity

@Entity
@Table(name = "files")
class File(
    @field:Column(
        name = "saved_name",
        nullable = false
    )
    var savedName: String,

    @field:Column(
        name = "origin_name",
        nullable = false
    )
    var originName: String
) : OnlyCreatedEntity()
