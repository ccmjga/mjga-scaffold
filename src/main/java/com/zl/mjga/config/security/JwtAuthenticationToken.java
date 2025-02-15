package com.zl.mjga.config.security;

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
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  @Serial private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  private final Object principal;

  private String credentials;

  public JwtAuthenticationToken(Object principal, String credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(false);
  }

  public JwtAuthenticationToken(
      Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true);
  }

  public static JwtAuthenticationToken unauthenticated(String userIdentify, String token) {
    return new JwtAuthenticationToken(userIdentify, token);
  }

  public static JwtAuthenticationToken authenticated(
      UserDetails principal, String token, Collection<? extends GrantedAuthority> authorities) {
    return new JwtAuthenticationToken(principal, token, authorities);
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
