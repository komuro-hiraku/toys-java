/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ast {

  public sealed interface Expression
      permits Assignment,
          BinaryExpression,
          BlockExpression,
          Identifier,
          IfExpression,
          IntegerLiteral,
          WhileExpression {}

  public static BinaryExpression add(Expression lhs, Expression rhs) {
    return new BinaryExpression(Operator.ADD, lhs, rhs);
  }

  public static BinaryExpression subtract(Expression lhs, Expression rhs) {
    return new BinaryExpression(Operator.SUBTRACT, lhs, rhs);
  }

  public static BinaryExpression multiply(Expression lhs, Expression rhs) {
    return new BinaryExpression(Operator.MULTIPLY, lhs, rhs);
  }

  public static BinaryExpression divide(Expression lhs, Expression rhs) {
    return new BinaryExpression(Operator.DIVIDE, lhs, rhs);
  }

  public static IntegerLiteral integer(int value) {
    return new IntegerLiteral(value);
  }

  public static BlockExpression Block(Expression... elements) {
    return new BlockExpression(Arrays.asList(elements));
  }

  // `while` が予約済みなので大文字開始でお茶濁し
  public static WhileExpression While(Expression condition, Expression body) {
    return new WhileExpression(condition, body);
  }

  public static IfExpression If(Expression condition, Expression thenClause) {
    return new IfExpression(condition, thenClause, Optional.empty());
  }

  ////// 以下Expression定義
  public static final record BinaryExpression(Operator operator, Expression lhs, Expression rhs)
      implements Expression {}

  public static final record IntegerLiteral(int value) implements Expression {}

  // 代入に必要な宣言
  public static final record Assignment(String name, Expression expression) implements Expression {}

  // 変数に必要な宣言
  public static final record Identifier(String name) implements Expression {}

  //// 制御構文関連

  public static final record BlockExpression(List<Expression> elements) implements Expression {}

  public static final record WhileExpression(Expression condition, Expression body)
      implements Expression {}

  public static final record IfExpression(
      Expression condition, Expression thenClause, Optional<Expression> elseClause)
      implements Expression {}

  //// Environment
  public static final record Environment(
      Map<String, Integer> bindings, Optional<Environment> next) {

    /**
     * パラメータ名を Environment の中から探す。 bindings から name が存在するかどうかをチェック。なければ next で findBindings を実行
     *
     * @param name 登録してあるパラメータ名
     * @return {@link Optional}
     */
    public Optional<Map<String, Integer>> findBinding(String name) {
      if (bindings.get(name) != null) {
        return Optional.of(bindings);
      }
      if (next.isPresent()) {
        return next.get().findBinding(name);
      } else {
        return Optional.empty();
      }
    }
  }
}
