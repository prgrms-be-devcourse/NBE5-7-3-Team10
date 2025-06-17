//package kr.co.programmers.collabond.api.recruit.interfaces;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import kr.co.programmers.collabond.api.profile.domain.dto.ProfileSimpleResponseDto;
//import kr.co.programmers.collabond.api.recruit.application.RecruitPostService;
//import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus;
//import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostRequestDto;
//import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostResponseDto;
//import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
//import kr.co.programmers.collabond.shared.exception.ApiExceptionHandler;
//import kr.co.programmers.collabond.shared.exception.custom.ForbiddenException;
//import kr.co.programmers.collabond.shared.exception.custom.NotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//class RecruitPostControllerTest {
//
//    private MockMvc mockMvc;
//    private ObjectMapper objectMapper;
//    private RecruitPostService recruitPostService;
//    private RecruitPostController recruitPostController;
//    private RecruitPostRequestDto requestDto;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule()); // ✅ LocalDateTime 직렬화 지원
//
//        recruitPostService = mock(RecruitPostService.class);
//        recruitPostController = new RecruitPostController(recruitPostService);
//
//        // ✅ ObjectMapper를 MessageConverter에 설정
//        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
//        messageConverter.setObjectMapper(objectMapper);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(recruitPostController)
//                .setControllerAdvice(new ApiExceptionHandler()) // ✅ 예외 처리기 연결
//                .setMessageConverters(messageConverter)  // ✅ 설정된 MessageConverter 사용
//                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
//                .build();
//
//        requestDto = new RecruitPostRequestDto();
//        requestDto.setProfileId(1L);
//        requestDto.setTitle("테스트 제목");
//        requestDto.setDescription("테스트 설명");
//        requestDto.setStatus("RECRUITING");
//        requestDto.setDeadline(LocalDateTime.now().plusDays(5));
//    }
//
//    @Test
//    @DisplayName("모집글 생성 성공")
//    void createRecruitPostSuccess() throws Exception {
//        RecruitPostResponseDto responseDto = buildDummyResponseDto();
//        when(recruitPostService.createRecruitPost(any(), any())).thenReturn(responseDto);
//
//        mockMvc.perform(post("/api/recruitments")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    @DisplayName("모집글 단건 조회 성공")
//    void getRecruitPostSuccess() throws Exception {
//        RecruitPostResponseDto responseDto = buildDummyResponseDto();
//        when(recruitPostService.getRecruitPostById(1L)).thenReturn(responseDto);
//
//        mockMvc.perform(get("/api/recruitments/1"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("모집글 단건 조회 실패 - NotFoundException")
//    void getRecruitPostNotFound() throws Exception {
//        when(recruitPostService.getRecruitPostById(999L)).thenThrow(new NotFoundException());
//
//        mockMvc.perform(get("/api/recruitments/999"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("모집글 수정 성공")
//    void updateRecruitPostSuccess() throws Exception {
//        RecruitPostResponseDto responseDto = buildDummyResponseDto();
//        when(recruitPostService.updateRecruitPost(eq(1L), any(), any())).thenReturn(responseDto);
//
//        mockMvc.perform(patch("/api/recruitments/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("모집글 수정 실패 - ForbiddenException")
//    void updateRecruitPostForbidden() throws Exception {
//        when(recruitPostService.updateRecruitPost(eq(1L), any(), any()))
//                .thenThrow(new ForbiddenException());
//
//        mockMvc.perform(patch("/api/recruitments/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("모집글 삭제 성공")
//    void deleteRecruitPostSuccess() throws Exception {
//        mockMvc.perform(delete("/api/recruitments/1"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("모집글 삭제 실패 - ForbiddenException")
//    void deleteRecruitPostForbidden() throws Exception {
//        doThrow(new ForbiddenException()).when(recruitPostService).deleteRecruitPost(eq(1L), any());
//
//        mockMvc.perform(delete("/api/recruitments/1"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("모집글 목록 조회 성공")
//    void getAllRecruitPostsSuccess() throws Exception {
//        RecruitPostResponseDto responseDto = buildDummyResponseDto();
//
//        when(recruitPostService.getAllRecruitPosts(any(), any(), any()))
//                .thenReturn(new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1));
//
//        mockMvc.perform(get("/api/recruitments")
//                        .param("status", RecruitPostStatus.RECRUITING.name())
//                        .param("sort", "createdAt"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 더미 DTO 빌더 메서드
//    private RecruitPostResponseDto buildDummyResponseDto() {
//        ProfileSimpleResponseDto profileDto = ProfileSimpleResponseDto.builder()
//                .profileId(10L)
//                .type("STORE")
//                .imageUrl("https://example.com/image.png")
//                .address("서울시 종로구")
//                .status(true)
//                .build();
//
//        return RecruitPostResponseDto.builder()
//                .id(1L)
//                .title("테스트 모집글")
//                .description("모집글 내용입니다.")
//                .status(RecruitPostStatus.RECRUITING)
//                .deadline(LocalDateTime.of(2025, 12, 31, 23, 59))
//                .profileId(10L)
//                .profileName("짱구")
//                .profile(profileDto)
//                .createdAt(LocalDateTime.of(2025, 6, 1, 12, 0))
//                .deletedAt(null)
//                .build();
//    }
//}