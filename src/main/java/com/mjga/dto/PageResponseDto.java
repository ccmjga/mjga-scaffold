package com.mjga.dto;

import jakarta.annotation.Nullable;
import lombok.*;

@Data
public class PageResponseDto<T> {
  private long total;
  private T data;

  public PageResponseDto(long total, @Nullable T data) {
    if (total < 0) {
      throw new IllegalArgumentException("total must not be less than zero");
    }
    this.total = total;
    this.data = data;
  }

  public static <T> PageResponseDto<T> empty() {
    return new PageResponseDto<>(0, null);
  }
}
