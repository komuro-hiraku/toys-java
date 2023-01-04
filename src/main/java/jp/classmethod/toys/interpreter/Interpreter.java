package jp.classmethod.toys.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Interpreter
 */
public class Interpreter {

    public final Map<String, Integer> environment;

    public Interpreter() {
        this.environment = new HashMap<>();
    }

    public int interpret(Ast.Expression expression) {

        if (expression instanceof Ast.BinaryExpression binaryExpression) {
            var lhs = interpret(binaryExpression.lhs());
            var rhs = interpret(binaryExpression.rhs());
            return switch (binaryExpression.operator()) {
                // 四則演算
                case ADD -> lhs + rhs;
                case SUBTRACT -> lhs - rhs;
                case MULTIPLY -> lhs * rhs;
                case DIVIDE -> lhs / rhs;

                // 比較式
                case LESS_THAN -> lhs > rhs ? 1 : 0;
                case LESS_OR_EQUAL -> lhs >= rhs ? 1 : 0;
                case GREATER_THAN -> lhs < rhs ? 1 : 0;
                case GREATER_OR_EQUAL -> lhs <= rhs ? 1 : 0;
                case EQUAL_EQUAL -> lhs == rhs ? 1 : 0;
                case NOT_EQUAL -> lhs != rhs ? 1 : 0;
            };
        } else if (expression instanceof Ast.IntegerLiteral integer) {
            return integer.value();
        } else if (expression instanceof Ast.Identifier e) {
            return environment.get(e.name());
        } else if (expression instanceof Ast.Assignment e) {
            int value = interpret(e.expression());
            environment.put(e.name(), value);
            return value;
        } else if (expression instanceof Ast.IfExpression e) {
            int condition = interpret(e.condition());
            if (condition != 0) {
                return interpret(e.thenClause());
            } else {
                var elseClauseOpt = e.elseClause();
                // Optional で存在しなかったら1を返す
                return elseClauseOpt.map(this::interpret).orElse(1);
            }
        } else if (expression instanceof Ast.WhileExpression e) {
            // 無限ループでひたすら評価
            while (true) {
                int condition = interpret(e.condition());   // condition 部を評価

                // 条件が真であれば body を評価
                if (condition != 0) {
                    interpret(e.body());
                } else {
                    // 条件が偽であれば脱出
                    break;
                }
            }
            // 直値
            return 1;
        } else if (expression instanceof Ast.BlockExpression e) {
            int value = 0;  // 初期化
            for (var element : e.elements()) {  // Block 内部を評価
                value = interpret(element);
            }
            return value;   // 最後に評価した値を返す
        } else {
            throw new RuntimeException(
                    "not reach here"
            );
        }
    }
}
