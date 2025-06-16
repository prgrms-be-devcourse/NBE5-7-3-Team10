package kr.co.programmers.collabond.api.apply.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.programmers.collabond.api.apply.application.ApplyPostService;
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto;
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto;
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto;
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto;
import kr.co.programmers.collabond.core.auth.jwt.TokenAuthenticationFilter;
import kr.co.programmers.collabond.core.config.SecurityConfig;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.custom.InvalidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplyPostController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {SecurityConfig.class, TokenAuthenticationFilter.class})
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ApplyPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplyPostService applyPostService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("지원서 제출 테스트")
    @WithMockUser
    void applyPost_success() throws Exception {
        // given
        Long recruitmentId = 1L;
        Long profileId = 2L;
        String content = "지원 내용입니다.";

        ApplyPostRequestDto requestDto = new ApplyPostRequestDto(profileId, content);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        MockMultipartFile jsonFile = new MockMultipartFile(
                "applyRequest",
                "",
                "application/json",
                requestJson.getBytes());

        MockMultipartFile attachment = new MockMultipartFile(
                "attachment",
                "test.pdf",
                "application/pdf",
                "test content".getBytes());

        ApplyPostDto responseDto = mock(ApplyPostDto.class);
        when(responseDto.getId()).thenReturn(1L);
        when(responseDto.getContent()).thenReturn(content);
        when(responseDto.getStatus()).thenReturn("PENDING");

        when(applyPostService.applyPost(
                eq(recruitmentId),
                any(ApplyPostRequestDto.class),
                any())
        ).thenReturn(responseDto);

        // when & then
        mockMvc.perform(multipart("/api/applications/{recruitmentId}", recruitmentId)
                        .file(jsonFile)
                        .file(attachment)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.content").value(responseDto.getContent()))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus()))
                .andDo(print());
    }

    @Test
    @DisplayName("지원서 제출 실패 테스트")
    @WithMockUser
    void applyPost_fail() throws Exception {
        // given
        Long recruitmentId = 1L;
        Long profileId = 2L;
        String content = "지원 내용입니다.";

        ApplyPostRequestDto requestDto = new ApplyPostRequestDto(profileId, content);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        MockMultipartFile jsonFile = new MockMultipartFile(
                "applyRequest",
                "",
                "application/json",
                requestJson.getBytes());

        MockMultipartFile attachment = new MockMultipartFile(
                "attachment",
                "test.pdf",
                "application/pdf",
                "test content".getBytes());

        ApplyPostDto responseDto = mock(ApplyPostDto.class);
        when(responseDto.getId()).thenReturn(1L);
        when(responseDto.getContent()).thenReturn(content);
        when(responseDto.getStatus()).thenReturn("PENDING");

        when(applyPostService.applyPost(
                eq(recruitmentId),
                any(ApplyPostRequestDto.class),
                any())
        ).thenThrow(new InvalidException(ErrorCode.REQUEST_TO_SAME_ROLE));

        // when & then
        mockMvc.perform(multipart("/api/applications/{recruitmentId}", recruitmentId)
                        .file(jsonFile)
                        .file(attachment)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.REQUEST_TO_SAME_ROLE.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("보낸 지원서 목록 조회 테스트")
    @WithMockUser
    void sentApplyPosts() throws Exception {
        // given
        List<ApplyPostDto> applyPosts = new ArrayList<>();

        ApplyPostDto post1 = mock(ApplyPostDto.class);
        when(post1.getId()).thenReturn(1L);
        when(post1.getStatus()).thenReturn("PENDING");

        ApplyPostDto post2 = mock(ApplyPostDto.class);
        when(post2.getId()).thenReturn(2L);
        when(post2.getStatus()).thenReturn("ACCEPTED");

        applyPosts.add(post1);
        applyPosts.add(post2);

        Page<ApplyPostDto> page = new PageImpl<>(applyPosts, PageRequest.of(0, 10), 1);

        when(applyPostService.findSentApplyPosts(
                        any(SentApplyPostsRequestDto.class),
                        any(),
                        any(Pageable.class)
                )
        ).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/applications/sent")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].status").value("ACCEPTED"))
                .andDo(print());
    }

    @Test
    @DisplayName("받은 지원서 목록 조회 테스트")
    @WithMockUser
    void receivedApplyPosts() throws Exception {
        // given
        List<ApplyPostDto> applyPosts = new ArrayList<>();

        ApplyPostDto post1 = mock(ApplyPostDto.class);
        when(post1.getId()).thenReturn(1L);
        when(post1.getStatus()).thenReturn("PENDING");

        ApplyPostDto post2 = mock(ApplyPostDto.class);
        when(post2.getId()).thenReturn(2L);
        when(post2.getStatus()).thenReturn("ACCEPTED");

        applyPosts.add(post1);
        applyPosts.add(post2);

        Page<ApplyPostDto> page = new PageImpl<>(applyPosts);

        when(applyPostService.findReceivedApplyPosts(
                        any(ReceivedApplyPostsRequestDto.class),
                        any(),
                        any(Pageable.class)
                )
        )
                .thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/applications/received")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].status").value("ACCEPTED"))
                .andDo(print());
    }

    @Test
    @DisplayName("지원서 수락 테스트")
    @WithMockUser
    void acceptApply() throws Exception {
        // given
        Long applicationId = 1L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/applications/accept/{applicationId}", applicationId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
