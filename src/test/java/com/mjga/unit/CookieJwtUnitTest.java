package com.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mjga.config.security.CookieJwt;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CookieJwtUnitTest {

  @Spy private CookieJwt cookieJwt = new CookieJwt("M3pIZlfyzkJ5Hi9OL", 60, "jwtCookieName");

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Test
  void createVerifyGetSubjectJwt_givenUserIdentify_shouldReturnTrueAndGetExpectIdentify() {
    String jwt = cookieJwt.createJwt("1");
    assertThat(cookieJwt.verifyToken(jwt)).isTrue();
    assertThat(cookieJwt.getSubject(jwt)).isEqualTo("1");
  }

  @Test
  void buildJwtCookiePojo_givenUserIdentify_shouldReturnExpectJwtCookie() {
    String jwt = cookieJwt.createJwt("1");
    Cookie stubCookie = new Cookie(cookieJwt.getCookieName(), jwt);
    stubCookie.setSecure(true);
    stubCookie.setPath("/");
    stubCookie.setMaxAge(cookieJwt.getExpirationMin() * 60);
    stubCookie.setHttpOnly(true);
    when(request.isSecure()).thenReturn(true);
    Cookie cookie = cookieJwt.buildJwtCookiePojo(request, "1");
    assertThat(cookie.equals(stubCookie)).isTrue();
  }
}
