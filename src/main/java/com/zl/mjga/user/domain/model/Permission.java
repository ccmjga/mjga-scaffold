package com.zl.mjga.user.domain.model;

import java.util.List;
import org.babyfish.jimmer.sql.*;

@Entity
@Table(name = "mjga.permission")
public interface Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long id();

  @Key
  String code();

  String name();

  @ManyToMany(mappedBy = "permissions")
  List<Role> roles();
}
