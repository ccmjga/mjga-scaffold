package com.mjga.integration.e2e;

import com.mjga.repository.UserRepository;
import com.mjga.repository.UserRoleMapRepository;
import java.time.Duration;
import org.jooq.generated.tables.pojos.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class SignE2ETest {

  @Value("${jwt.cookie-name}")
  private String jwtCookieName;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-min}")
  private int expirationMin;

  @Autowired private WebTestClient webTestClient;

  @Autowired private UserRepository userRepository;

  @Autowired private UserRoleMapRepository userRoleMapRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private TestRestTemplate testRestTemplate;

  @AfterEach
  void cleanUp() {
    User user = userRepository.fetchOneByUsername("test_5fab32c22a3e");
    userRoleMapRepository.deleteByUserId(user.getId());
    userRepository.deleteByUsername("test_5fab32c22a3e");
  }

  @Test
  void signUp() {
    webTestClient
        .post()
        .uri("/auth/sign-up")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
{
  "username": "test_5fab32c22a3e",
  "password": "test_eab28b939ba1"
}
""")
        .exchange()
        .expectStatus()
        .isCreated();
  }

  @Test
  void signIn() {
    User stubUser = new User();
    stubUser.setUsername("test_5fab32c22a3e");
    stubUser.setPassword(passwordEncoder.encode("test_eab28b939ba1"));
    userRepository.insert(stubUser);
    webTestClient
        .post()
        .uri("/auth/sign-in")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
{
  "username": "test_5fab32c22a3e",
  "password": "test_eab28b939ba1"
}
""")
        .exchange()
        .expectCookie()
        .exists(jwtCookieName)
        .expectCookie()
        .maxAge(jwtCookieName, Duration.ofSeconds(expirationMin * 60L))
        .expectStatus()
        .isOk();
  }

  @Test
  void signOut() {
    User stubUser = new User();
    stubUser.setUsername("test_5fab32c22a3e");
    stubUser.setPassword(passwordEncoder.encode("test_eab28b939ba1"));
    userRepository.insert(stubUser);
    User loginUser = new User();
    loginUser.setUsername("test_5fab32c22a3e");
    loginUser.setPassword("test_eab28b939ba1");
    HttpHeaders headers =
        testRestTemplate.postForEntity("/auth/sign-in", loginUser, String.class).getHeaders();
    headers
        .get("Set-Cookie")
        .forEach(
            cookie -> {
              if (cookie.startsWith(jwtCookieName)) {
                webTestClient
                    .post()
                    .uri("/auth/sign-out")
                    .header("Cookie", cookie)
                    .exchange()
                    .expectCookie()
                    .maxAge(jwtCookieName, Duration.ofSeconds(0L))
                    .expectStatus()
                    .isOk();
              }
            });
  }
}
