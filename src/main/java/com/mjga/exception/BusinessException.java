package com.mjga.exception;

public class BusinessException extends RuntimeException {

  @java.io.Serial private static final long serialVersionUID = -2119302295305964305L;

  public BusinessException() {}

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }

  public BusinessException(Throwable cause) {
    super(cause);
  }

  public BusinessException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
