package com.mjga.dto.urp;

import java.util.LinkedList;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleDto {
  private Long id;
  private String code;
  private String name;
  List<PermissionDto> permissions = new LinkedList<>();
}
