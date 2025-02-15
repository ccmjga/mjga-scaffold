package com.zl.mjga.user.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import org.babyfish.jimmer.sql.*;

@Entity
@Table(name = "mjga.user")
public interface User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long id();

  @Key
  String username();

  LocalDateTime createTime();

  String password();

  boolean enable();

  @ManyToMany
  @JoinTable(
      name = "mjga.user_role_map",
      joinColumnName = "user_id",
      inverseJoinColumnName = "role_id")
  List<Role> roles();
}
