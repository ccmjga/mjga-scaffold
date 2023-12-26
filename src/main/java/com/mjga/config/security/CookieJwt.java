package com.mjga.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;


@Slf4j
@Component
@Getter
public class CookieJwt {

  private final String secret;

  private final int expirationMin;

  private final String cookieName;

  private final JWTVerifier verifier;

  public CookieJwt(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-min}") int expirationMin,
      @Value("${jwt.cookie-name}") String cookieName) {
    this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
    this.secret = secret;
    this.expirationMin = expirationMin;
    this.cookieName = cookieName;
  }

  public String getSubject(String token) {
    return JWT.decode(token).getSubject();
  }

  public Boolean verifyToken(String token) {
    try {
      verifier.verify(token);
      return Boolean.TRUE;
    } catch (JWTVerificationException e) {
      return Boolean.FALSE;
    }
  }

  public String createJwt(String userIdentify) {
    return JWT.create()
        .withSubject(String.valueOf(userIdentify))
        .withIssuedAt(new Date())
        .withExpiresAt(
            Date.from(
                LocalDateTime.now()
                    .plusMinutes(expirationMin)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()))
        .sign(Algorithm.HMAC256(secret));
  }

  public void createJwtCookie(
      HttpServletRequest request, HttpServletResponse response, String userIdentify) {
    response.addCookie(buildJwtCookiePojo(request, userIdentify));
  }

  public Cookie buildJwtCookiePojo(HttpServletRequest request, String userIdentify) {
    String contextPath = request.getContextPath();
    String cookiePath = StringUtils.hasText(contextPath) ? contextPath : "/";
    Cookie cookie = new Cookie(cookieName, createJwt(userIdentify));
    cookie.setPath(cookiePath);
    cookie.setMaxAge(expirationMin * 60);
    cookie.setSecure(request.isSecure());
    cookie.setHttpOnly(true);
    return cookie;
  }

  public String extractJwt(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  public void removeJwtCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = WebUtils.getCookie(request, cookieName);
    if (cookie != null) {
      cookie.setMaxAge(0);
    }
    response.addCookie(cookie);
  }
}
