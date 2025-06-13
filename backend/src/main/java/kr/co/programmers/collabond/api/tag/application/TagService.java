package kr.co.programmers.collabond.api.tag.application;

import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.profiletag.domain.ProfileTag;
import kr.co.programmers.collabond.api.profiletag.interfaces.ProfileTagMapper;
import kr.co.programmers.collabond.api.tag.domain.Tag;
import kr.co.programmers.collabond.api.tag.domain.TagType;
import kr.co.programmers.collabond.api.tag.infrastructure.TagRepository;
import kr.co.programmers.collabond.api.tag.domain.dto.TagResponseDto;
import kr.co.programmers.collabond.api.tag.domain.dto.TagRequestDto;
import kr.co.programmers.collabond.api.tag.interfaces.TagMapper;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.custom.DuplicatedException;
import kr.co.programmers.collabond.shared.exception.InvalidException;
import kr.co.programmers.collabond.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    // 태그 생성
    @Transactional
    public TagResponseDto create(TagRequestDto dto) {
        // 중복된 태그 이름 체크

        if (tagRepository.existsByNameAndType(dto.name(), TagType.valueOf(dto.type()))) {
            throw new DuplicatedException(ErrorCode.DUPLICATED_TAG);
        }

        // Tag 엔티티로 변환
        Tag tag = TagMapper.toEntity(dto);
        Tag savedTag = tagRepository.save(tag);
        return TagMapper.toDto(savedTag); // DTO 변환 후 반환
    }

    // 태그 삭제
    @Transactional
    public void delete(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TAG_NOT_FOUND));
        tagRepository.delete(tag);
    }

    // 모든 태그 조회
    @Transactional(readOnly = true)
    public List<TagResponseDto> findAll() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(TagMapper::toDto).toList(); // Tag -> TagResponseDto 변환 후 반환
    }

    //프로필에 태그를 설정 (최대 5개, 타입 일치 검증)
    @Transactional
    public void validateAndBindTags(Profile profile, List<Long> tagIds) {
        if (tagIds.size() > 5) {
            throw new InvalidException(ErrorCode.OVER_MAX_TAG);
        }

        List<Tag> tags = tagRepository.findAllById(tagIds);

        if (tags.size() != tagIds.size()) {
            throw new NotFoundException(ErrorCode.INCLUDE_TAG_NOT_FOUND);
        }

        TagType profileType = TagType.valueOf(profile.getType().name());
        for (Tag tag : tags) {
            if (!tag.getType().equals(profileType)) {
                throw new InvalidException(ErrorCode.NOT_MATCH_TYPE_OF_TAG);
            }
        }

        for (Tag tag : tags) {
            ProfileTag profileTag = ProfileTagMapper.toEntity(tag);
            profile.addTag(profileTag); // Profile이 연관관계 관리
        }
    }

    //프로필에 연결된 태그 전부 제거
    @Transactional
    public void clearTags(Profile profile) {
        profile.getTags().clear();// orphanRemoval로 자동 삭제
    }
}
