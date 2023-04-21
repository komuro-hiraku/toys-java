/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.parser;

import static org.javafp.parsecj.Text.*;

import java.util.function.BinaryOperator;
import jp.classmethod.toys.interpreter.Ast;
import org.javafp.data.Unit;
import org.javafp.parsecj.Parser;

public class Parsers {

  /**
   * `wspace` はスペース、改行、タブを示す. 合致したら Unit へ変換. `regex((?m)//.*$` はシングルラインコメントを示す。合致したら Unit へ変換.
   * `(?m)` はマルチラインモードだそう. `regex` は ParsecJ の組み込みのものを使う.
   */
  public static final Parser<Character, Unit> SPACING =
      wspace.map(_1 -> Unit.unit).or(regex("(?m)//.*$").map(__1 -> Unit.unit));

  public static final Parser<Character, Unit> SPACINGS = SPACING.many().map(__1 -> Unit.unit);
  public static final Parser<Character, Unit> PLUS = string("+").then(SPACINGS);
  public static final Parser<Character, Unit> MINUS = string("-").then(SPACINGS);
  public static final Parser<Character, Unit> ASTER = string("*").then(SPACINGS);
  public static final Parser<Character, Unit> SLASH = string("/").then(SPACINGS);
  public static final Parser<Character, Unit> LPAREN = string("(").then(SPACINGS);
  public static final Parser<Character, Unit> RPAREN = string(")").then(SPACINGS);
  public static final Parser<Character, Unit> COMMA = string(",").then(SPACINGS);
  public static final Parser<Character, Unit> LBRACKET = string("[").then(SPACINGS);
  public static final Parser<Character, Unit> RBRACKET = string("]").then(SPACINGS);
  public static final Parser<Character, Unit> EQ = string("=").then(SPACINGS);
  public static final Parser<Character, Unit> TRUE = string("true").then(SPACINGS);
  public static final Parser<Character, Unit> FALSE = string("false").then(SPACINGS);
  public static final Parser<Character, String> IDENT =
      regex("[a-zA-Z_][a-zA-Z0-9_]*").bind(name -> SPACINGS.map(__ -> name));
  public static final Parser<Character, Ast.IntegerLiteral> integer =
      intr // 数値の文字列を合致させる
          .map(Ast::integer) // Ast::integer で IntegerLiteral に変換
          .bind(v -> SPACINGS.map(__ -> v)); // ここの Operation がよくわかっていない???:

  // expression <- additive;
  public static Parser<Character, Ast.Expression> expression() {
    return additive();
  }

  // additive <- multitive
  //     ( '+' multitive / '-' multitive)*;

  public static Parser<Character, Ast.Expression> additive() {
    Parser<Character, BinaryOperator<Ast.Expression>> add = PLUS.map(op -> Ast::add);
    Parser<Character, BinaryOperator<Ast.Expression>> sub = MINUS.map(op -> Ast::subtract);

    return multitive().chainl1(add.or(sub));
  }

  public static Parser<Character, Ast.Expression> multitive() {
    Parser<Character, BinaryOperator<Ast.Expression>> mul = ASTER.map(op -> Ast::multiply);
    Parser<Character, BinaryOperator<Ast.Expression>> div = SLASH.map(op -> Ast::divide);

    return primary().chainl1(mul.or(div));
  }

  public static Parser<Character, Ast.Expression> primary() {
    return LPAREN
        .bind(_1 -> expression().bind(v -> RPAREN.map(_2 -> v)))
        .or(integer)
        .or(functionCall())
        .or(labelledCall())
        .or(arrayLiteral())
        .or(boolLiteral())
        .or(identifier());
  }

  // Example func(0, "fuga")
  public static Parser<Character, Ast.FunctionCall> functionCall() {
    return IDENT
        .bind(
            name ->
                expression()
                    .sepBy(COMMA) // separate
                    .between(LPAREN, RPAREN) // ( and )
                    .map(params -> new Ast.FunctionCall(name, params.toList())) // 引数を List にして呼び出し
            )
        .attempt(); // ???
  }

  // Example func[num = 0, str = "fuga"]
  public static Parser<Character, Ast.LabelledCall> labelledCall() {
    return IDENT
        .bind(
            name ->
                IDENT
                    .bind(
                        label ->
                            EQ.then(expression())
                                .map(param -> new Ast.LabelledParameter(label, param)))
                    .sepBy(COMMA)
                    .between(LBRACKET, RBRACKET)
                    .map(params -> new Ast.LabelledCall(name, params.toList())))
        .attempt();
  }

  // Example [0, 1, 2, 3, 4]
  public static Parser<Character, Ast.ArrayLiteral> arrayLiteral() {
    return LBRACKET.bind(
        __1 ->
            expression()
                .sepBy(COMMA)
                .bind(params -> RBRACKET.map(__2 -> new Ast.ArrayLiteral(params.toList()))));
  }

  public static Parser<Character, Ast.Identifier> identifier() {
    return IDENT.map(Ast.Identifier::new);
  }

  public static Parser<Character, Ast.BoolLiteral> boolLiteral() {
    return TRUE.map(__ -> new Ast.BoolLiteral(true))
        .or(FALSE.map(__ -> new Ast.BoolLiteral(false)));
  }
}
