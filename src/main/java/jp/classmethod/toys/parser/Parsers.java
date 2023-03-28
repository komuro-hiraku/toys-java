package jp.classmethod.toys.parser;

import jp.classmethod.toys.interpreter.Ast;
import org.javafp.data.Unit;
import org.javafp.parsecj.Parser;

import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Text.*;

public class Parsers {

    /**
     * `wspace` はスペース、改行、タブを示す. 合致したら Unit へ変換.
     * `regex((?m)//.*$` はシングルラインコメントを示す。合致したら Unit へ変換.
     * `(?m)` はマルチラインモードだそう. `regex` は ParsecJ の組み込みのものを使う.
     */
    public static final Parser<Character, Unit> SPACING =
            wspace.map(_1 -> Unit.unit).or(
                    regex("(?m)//.*$").map(__1 -> Unit.unit));
    public static final Parser<Character, Unit> SPACINGS =
            SPACING.many().map(__1 -> Unit.unit);
    public static final Parser<Character, Unit> PLUS = string("+").then(SPACINGS);
    public static final Parser<Character, Unit> MINUS = string("-").then(SPACINGS);
    public static final Parser<Character, Unit> ASTER = string("*").then(SPACINGS);
    public static final Parser<Character, Unit> SLASH = string("/").then(SPACINGS);
    public static final Parser<Character, Unit> LPAREN = string("(").then(SPACINGS);
    public static final Parser<Character, Unit> RPAREN = string(")").then(SPACINGS);
    public static final Parser<Character, Ast.IntegerLiteral> integer =
        intr                                            // 数値の文字列を合致させる
                    .map(Ast::integer)                  // Ast::integer で IntegerLiteral に変換
                    .bind(v -> SPACINGS.map(__ -> v));  // ここの Operation がよくわかっていない???:

    // expression <- additive;
    public static Parser<Character, Ast.Expression> expression() {return additive();}

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
        return LPAREN.bind(_1 -> expression().bind(v -> RPAREN.map(_2 -> v))).or(integer);
    }
}
