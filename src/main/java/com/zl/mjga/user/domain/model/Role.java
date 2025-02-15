package com.zl.mjga.user.domain.model;

import java.util.List;
import org.babyfish.jimmer.sql.*;

@Entity
@Table(name = "mjga.role")
public interface Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long id();

  @Key
  String code();

  String name();

  @ManyToMany(mappedBy = "roles")
  List<User> users();

  @ManyToMany
  @JoinTable(
      name = "mjga.role_permission_map",
      joinColumnName = "role_id",
      inverseJoinColumnName = "permission_id")
  List<Permission> permissions();
}
