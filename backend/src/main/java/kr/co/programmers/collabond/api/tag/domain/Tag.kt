package kr.co.programmers.collabond.api.tag.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.shared.domain.CreatedEntity
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "tags")
@SQLDelete(sql = "UPDATE tags SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class Tag() : CreatedEntity() {

    @Column(nullable = false)
    lateinit var name: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var type: TagType

    constructor(name: String, type: TagType) : this() {
        this.name = name
        this.type = type
    }

    fun update(name: String?): Tag {
        name?.let { this.name = it }
        return this
    }
}
