/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.interpreter;

// https://github.com/toys-lang/toys/blob/master/src/main/java/com/github/kmizu/toys/LanguageException.java
public class LanguageException extends RuntimeException {
  public LanguageException(String s) {
    super(s);
  }
}
