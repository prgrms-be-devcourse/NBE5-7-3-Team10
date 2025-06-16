package kr.co.programmers.collabond.api.apply.application;

import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@SpringBootTest
class ApplyPostServiceTest {

    @Autowired
    ApplyPostService applyPostService;

    @Test
    @DisplayName("apply_post")
    void apply_test() throws Exception {

        //todo : profile이 있어야 동작함

        ApplyPostRequestDto applyPost = new ApplyPostRequestDto("내용");

        MultipartFile multipartFile1 = new MockMultipartFile("test1", // 파일의 파라미터 이름
                "spring1.png", // 실제 파일 이름
                "image/png", // 파일의 확장자 타입
                (byte[]) null
        );
        MultipartFile multipartFile2 = new MockMultipartFile("test2", // 파일의 파라미터 이름
                "spring2.png", // 실제 파일 이름
                "image/png", // 파일의 확장자 타입
                (byte[]) null
        );

        ArrayList<MultipartFile> files = new ArrayList<>();

        files.add(multipartFile1);
        files.add(multipartFile2);

        applyPostService.applyPost(0L, applyPost, files);
    }

}