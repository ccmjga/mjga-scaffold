package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.zl.mjga.config.security.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JwtUnitTest {

  @Spy private Jwt jwt = new Jwt("M3pIZlfyzkJ5Hi9OL", 60, "jwtCookieName");

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Test
  void createVerifyGetSubjectJwt_givenUserIdentify_shouldReturnTrueAndGetExpectIdentify() {
    String token = jwt.create("1");
    assertThat(jwt.verify(token)).isTrue();
    assertThat(jwt.getSubject(token)).isEqualTo("1");
  }

  @Test
  void getSubject_whenTokenIsInvalid_shouldThrowJWTDecodeException() {
    String invalidToken = "invalid.token.here";
    assertThatThrownBy(() -> jwt.getSubject(invalidToken)).isInstanceOf(JWTDecodeException.class);
  }

  @Test
  void getSubject_whenTokenHasDifferentSecret_shouldReturnSubject() {
    Jwt otherJwt = new Jwt("different_secret", 60, "cookie");
    String token = otherJwt.create("user123");

    assertThat(jwt.verify(token)).isFalse();
    assertThat(jwt.getSubject(token)).isEqualTo("user123");
  }

  @Test
  void getSubject_whenTokenIsNull_shouldThrowException() {
    assertThatThrownBy(() -> jwt.getSubject(null)).isInstanceOf(JWTDecodeException.class);
  }

  @Test
  void create_WithVariousUserIdentifiers_ShouldCorrectlySetSubject() {
    String[] identifiers = {"", "user@domain.com", "12345", "!@#$%"};
    for (String id : identifiers) {
      String token = jwt.create(id);
      assertThat(jwt.getSubject(token)).isEqualTo(id);
    }
  }

  @Test
  void create_withDifferentSecret_shouldFailVerification() {
    Jwt otherJwt = new Jwt("different_secret", 60, "cookie");
    String token = otherJwt.create("user");

    assertThat(jwt.verify(token)).isFalse();
  }

  @Test
  void create_WhenExpirationMinIsZero_shouldExpireImmediately() {
    Jwt zeroExpirationJwt = new Jwt("secret", 0, "cookie");
    String token = zeroExpirationJwt.create("test");
    DecodedJWT decoded = JWT.decode(token);

    assertThat(decoded.getExpiresAt()).isEqualTo(decoded.getIssuedAt());
  }
}
