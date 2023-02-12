/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.interpreter;

import java.util.List;
import java.util.Map;

public class Values {

  public sealed interface Value permits Int, Bool, Array, Dictionary {
    default Int asInt() {
      return (Int) this;
    }

    default Bool asBool() {
      return (Bool) this;
    }

    default Array asArray() {
      return (Array) this;
    }

    default Dictionary asDictionary() {
      return (Dictionary) this;
    }
  }

  public static final record Int(int value) implements Value {}

  public static final record Bool(boolean value) implements Value {}

  public static final record Array(List<? extends Value> values) implements Value {}

  public static final record Dictionary(Map<? extends Value, ? super Value> entries)
      implements Value {}

  public static Value wrap(Object javaValue) {
    if (javaValue instanceof Integer val) {
      return new Int(val);
    }
    if (javaValue instanceof Boolean val) {
      return new Bool(val);
    }
    if (javaValue instanceof List<?> val) {
      return new Array((List<Value>) val);
    }
    if (javaValue instanceof Map<?, ?> val) {
      return new Dictionary((Map<Value, Value>) val);
    }
    throw new LanguageException("must not reach here");
  }
}
