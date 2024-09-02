package com.mjga.config.security;

import java.io.Serial;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;

@Setter
@Getter
@ToString
public class CookieJwtAuthenticationToken extends AbstractAuthenticationToken {

  @Serial private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  private final Object principal;

  private String credentials;

  public CookieJwtAuthenticationToken(Object principal, String credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(false);
  }

  public CookieJwtAuthenticationToken(
      Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true);
  }

  public static CookieJwtAuthenticationToken unauthenticated(String userIdentify, String token) {
    return new CookieJwtAuthenticationToken(userIdentify, token);
  }

  public static CookieJwtAuthenticationToken authenticated(
      UserDetails principal, String token, Collection<? extends GrantedAuthority> authorities) {
    return new CookieJwtAuthenticationToken(principal, token, authorities);
  }

  @Override
  public String getCredentials() {
    return this.credentials;
  }

  @Override
  public Object getPrincipal() {
    return this.principal;
  }
}
