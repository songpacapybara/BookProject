package com.jojoldu.book.springboot.web;

import com.jojoldu.book.springboot.domain.posts.Posts;
import com.jojoldu.book.springboot.domain.posts.PostsRepository;
import com.jojoldu.book.springboot.web.dto.PostsSaveRequestDto;
import com.jojoldu.book.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class    PostsApiControllerTest {

    @LocalServerPort
    private int port; // 테스트가 실행될 서버의 포트 번호

    @Autowired
    private TestRestTemplate restTemplate; // REST API를 호출하기 위한 템플릿

    @Autowired
    private PostsRepository postsRepository; // JPA 레포지토리, DB와의 상호작용을 위한 것

    @After
    public void tearDown() throws Exception {
        // 각 테스트가 끝난 후, 데이터베이스를 초기화하여 다음 테스트에 영향을 주지 않도록 함
        postsRepository.deleteAll();
    }

    @Test
    public void Posts_등록된다() throws Exception {
        // given: 테스트를 위한 데이터 준비
        String title = "title";
        String content = "content";
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author("author")
                .build();

        // 테스트할 URL 설정
        String url = "http://localhost:" + port + "/api/v1/posts";

        // when: REST API 호출
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);

        // then: 결과 검증
        // API 호출의 상태 코드가 200 OK인지 확인
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        // API 호출의 응답 본문이 0보다 큰지 확인 (등록된 Post의 ID)
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        // 데이터베이스에서 모든 Post를 조회
        List<Posts> all = postsRepository.findAll();
        // 데이터베이스에 저장된 Post의 title과 content가 예상한 값과 일치하는지 확인
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }

    @Test
    public void Posts_수정된다() throws Exception {
        // given: 테스트를 위한 데이터 준비 및 저장
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title("title")
                .content("content")
                .author("author")
                .build());
        Long updateId = savedPosts.getId(); // 저장된 Post의 ID를 얻음
        String expectedTitle = "title2";
        String expectedContent = "content2";

        // 수정할 데이터 DTO 생성
        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                .title(expectedTitle)
                .content(expectedContent)
                .build();

        // 테스트할 URL 설정
        String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        // HttpEntity를 사용하여 PUT 요청을 위한 요청 본문을 설정
        HttpEntity<PostsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

        // when: REST API 호출 (PUT 요청)
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Long.class);

        // then: 결과 검증
        // API 호출의 상태 코드가 200 OK인지 확인
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        // API 호출의 응답 본문이 0보다 큰지 확인 (수정된 Post의 ID)
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        // 데이터베이스에서 모든 Post를 조회
        List<Posts> all = postsRepository.findAll();
        // 수정된 Post의 title과 content가 예상한 값과 일치하는지 확인
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
    }
}