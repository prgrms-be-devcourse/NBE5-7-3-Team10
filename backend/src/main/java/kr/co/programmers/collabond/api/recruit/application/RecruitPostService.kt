package kr.co.programmers.collabond.api.recruit.application

import kr.co.programmers.collabond.api.profile.application.ProfileService
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.recruit.domain.dto.Requests
import kr.co.programmers.collabond.api.recruit.domain.dto.Responses
import kr.co.programmers.collabond.api.recruit.infrastructure.RecruitPostRepository
import kr.co.programmers.collabond.api.recruit.interfaces.RecruitPostMapper
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.ForbiddenException
import kr.co.programmers.collabond.shared.exception.InvalidException
import kr.co.programmers.collabond.shared.exception.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecruitPostService(
    private val profileService: ProfileService,
    private val userService: UserService,
    private val recruitPostRepository: RecruitPostRepository
) {

    @Transactional
    fun createRecruitPost(
        request: Requests,
        userInfo: OAuth2UserInfo
    ): Responses {
        val profile = profileService.findByProfileId(request.profileId)
        val loginUser = userService.findByProviderId(userInfo.username)

        if (profile.user.id != loginUser.id) throw ForbiddenException()

        val post = RecruitPostMapper.toEntity(request, profile)
        val savedPost = recruitPostRepository.save(post)

        return RecruitPostMapper.toResponseDto(savedPost);
    }

    @Transactional
    fun updateRecruitPost(
        recruitmentId: Long,
        request: Requests,
        userInfo: OAuth2UserInfo
    ): Responses {
        val post = findRecruitPostById(recruitmentId)

        // 소프트 삭제된 게시글은 수정할 수 없습니다.
        if (post.deletedAt != null) throw InvalidException(ErrorCode.REMOVED_RECRUIT_POST)

        val loginUser = userService.findByProviderId(userInfo.username)
        if (post.profile?.user?.id != loginUser.id) throw ForbiddenException()

        post.update(
            title = request.title,
            description = request.description,
            status = request.status?.let(RecruitPostStatus::valueOf),
            deadline = request.deadline
        )

        return RecruitPostMapper.toResponseDto(post)
    }

    @Transactional
    fun deleteRecruitPost(recruitmentId: Long, userInfo: OAuth2UserInfo) {
        val post = findRecruitPostById(recruitmentId)
        val loginUser = userService.findByProviderId(userInfo.username)

        if (post.profile?.user?.id != loginUser.id) throw ForbiddenException()

        recruitPostRepository.delete(post)
    }

    @Transactional(readOnly = true)
    fun getAllRecruitPosts(
        status: RecruitPostStatus? = null,
        sort: String? = null,
        pageable: Pageable
    ): Page<Responses> {
        val posts = status?.let { recruitPostRepository.findByStatus(it, pageable) }
            ?: recruitPostRepository.findAll(pageable)

        return posts.map { recruitPost ->
            RecruitPostMapper.toResponseDto(recruitPost)
        }
    }

    @Transactional(readOnly = true)
    fun getRecruitPostsByUser(userId: Long, pageable: Pageable): Page<Responses> =
        recruitPostRepository.findByUserId(userId, pageable)
            .map { recruitPost ->
                RecruitPostMapper.toResponseDto(recruitPost)
            }

    @Transactional(readOnly = true)
    fun getRecruitPostByProfile(profileId: Long, pageable: Pageable): Page<Responses> =
        recruitPostRepository.findByProfileId(profileId, pageable)
            .map { recruitPost ->
                RecruitPostMapper.toResponseDto(recruitPost)
            }

    @Transactional(readOnly = true)
    fun findByRecruitmentId(recruitmentId: Long): RecruitPost = findRecruitPostById(recruitmentId)

    @Transactional(readOnly = true)
    fun getRecruitPostById(recruitmentId: Long): Responses {
        val recruitPost = findRecruitPostById(recruitmentId)
        return RecruitPostMapper.toResponseDto(recruitPost)
    }

    private fun findRecruitPostById(recruitmentId: Long): RecruitPost =
        recruitPostRepository.findByIdOrNull(recruitmentId)
            ?: throw NotFoundException(ErrorCode.RECRUIT_NOT_FOUND)

    private fun RecruitPost.getProfileImgName(): String =
        profile?.images?.firstOrNull { it.type == "PROFILE" }?.file?.savedName.orEmpty()
}