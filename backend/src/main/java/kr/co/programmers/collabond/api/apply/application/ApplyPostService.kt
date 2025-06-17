package kr.co.programmers.collabond.api.apply.application

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.apply.domain.ApplyPostStatus
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto
import kr.co.programmers.collabond.api.apply.infrastructure.ApplyPostRepository
import kr.co.programmers.collabond.api.apply.interfaces.ApplyPostMapper.toDto
import kr.co.programmers.collabond.api.apply.interfaces.ApplyPostMapper.toEntity
import kr.co.programmers.collabond.api.attachment.interfaces.AttachmentMapper
import kr.co.programmers.collabond.api.file.application.FileService
import kr.co.programmers.collabond.api.mail.service.MailService
import kr.co.programmers.collabond.api.profile.application.ProfileService
import kr.co.programmers.collabond.api.recruit.application.RecruitPostService
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.ForbiddenException
import kr.co.programmers.collabond.shared.exception.InvalidException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ApplyPostService(
    private val recruitPostService: RecruitPostService,
    private val profileService: ProfileService,
    private val userService: UserService,
    private val fileService: FileService,
    private val mailService: MailService,
    private val applyPostRepository: ApplyPostRepository
) {

    @Transactional
    fun applyPost(
        recruitmentId: Long,
        request: ApplyPostRequestDto,
        files: List<MultipartFile>?
    ): ApplyPostDto {

        val recruitPost = recruitPostService.findByRecruitmentId(recruitmentId)
        val profile = profileService.findByProfileId(request.profileId)

        if (recruitPost.profile!!.type == profile.type) {
            throw InvalidException(ErrorCode.REQUEST_TO_SAME_ROLE)
        }

        val applyPost = toEntity(recruitPost, profile, request)

        files?.takeIf { it.isNotEmpty() }?.let {
            val attachments = fileService.saveFiles(it).map { file ->
                AttachmentMapper.toEntity(applyPost, file)
            }.toMutableList()
            applyPost.updateAttachment(attachments)
        }

        val saved = applyPostRepository.save(applyPost)
        mailService.sendReceivedApplyMail(recruitPost, profile, saved.createdAt)

        return toDto(saved)
    }

    @Transactional(readOnly = true)
    fun findSentApplyPosts(
        request: SentApplyPostsRequestDto,
        userInfo: OAuth2UserInfo,
        pageable: Pageable
    ): Page<ApplyPostDto> {

        val user = userService.findByProviderId(userInfo.username)

        return when (request.status) {
            null -> applyPostRepository.findAllSentByUser(user.id, pageable)
            else -> applyPostRepository.findAllSentByUserIdAndStatus(
                user.id,
                request.status,
                pageable
            )
        }
    }

    @Transactional(readOnly = true)
    fun findReceivedApplyPosts(
        request: ReceivedApplyPostsRequestDto,
        userInfo: OAuth2UserInfo,
        pageable: Pageable
    ): Page<ApplyPostDto> {

        val user = userService.findByProviderId(userInfo.username)

        return when (request.status) {
            null -> applyPostRepository.findAllReceivedByUser(user.id, pageable)
            else -> applyPostRepository.findAllReceivedByUserIdAndStatus(
                user.id,
                request.status,
                pageable
            )
        }
    }

    @Transactional
    fun acceptApply(
        applicationId: Long,
        userInfo: OAuth2UserInfo
    ) {

        val receivedApply: ApplyPost = applyPostRepository.findByIdOrNull(applicationId)
            ?: throw InvalidException(ErrorCode.APPLY_NOT_FOUND)

        val loginUser = userService.findByProviderId(userInfo.username)

        if (receivedApply.recruitPost.profile!!.user.id != loginUser.id) {
            throw ForbiddenException(ErrorCode.FORBIDDEN_REQUEST)
        }

        receivedApply.apply {
            updateStatus(ApplyPostStatus.ACCEPTED)
            updateCollaboCount(this)
        }

        mailService.sendBondCompletionEmails(receivedApply, LocalDateTime.now())
    }

    private fun updateCollaboCount(
        applyPost: ApplyPost
    ) {
        applyPost.profile.updateCollaboCount()
        applyPost.recruitPost.profile!!.updateCollaboCount()
    }
}
