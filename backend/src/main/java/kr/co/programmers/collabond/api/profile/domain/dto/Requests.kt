package kr.co.programmers.collabond.api.profile.domain.dto


data class ProfileRequestDto(
    val userId: Long,
    val type: String,
    val name: String,
    val description: String,
    val address: String,
    val addressCode: String,
    val status: Boolean,
    val tagIds: List<Long>
)
