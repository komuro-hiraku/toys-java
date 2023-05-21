/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Interpreter */
public class Interpreter {

  private Ast.Environment variableEnvironment;

  private final Map<String, Ast.FunctionDefinition> functionEnvironment;

  public Interpreter() {
    this.functionEnvironment = new HashMap<>();
    variableEnvironment = newEnvironment(Optional.empty());
  }

  public void reset() {
    variableEnvironment = newEnvironment(Optional.empty());
    functionEnvironment.clear();
  }

  private static Ast.Environment newEnvironment(Optional<Ast.Environment> next) {
    return new Ast.Environment(new HashMap<>(), next);
  }

  public Values.Value interpret(Ast.Expression expression) {

    if (expression instanceof Ast.BinaryExpression binaryExpression) {
      var lhs = interpret(binaryExpression.lhs()).asInt().value();
      var rhs = interpret(binaryExpression.rhs()).asInt().value();
      return switch (binaryExpression.operator()) {
          // 四則演算
        case ADD -> Values.wrap(lhs + rhs);
        case SUBTRACT -> Values.wrap(lhs - rhs);
        case MULTIPLY -> Values.wrap(lhs * rhs);
        case DIVIDE -> Values.wrap(lhs / rhs);

          // 比較式
        case LESS_THAN -> Values.wrap(lhs < rhs);
        case LESS_OR_EQUAL -> Values.wrap(lhs <= rhs);
        case GREATER_THAN -> Values.wrap(lhs > rhs);
        case GREATER_OR_EQUAL -> Values.wrap(lhs >= rhs);
        case EQUAL_EQUAL -> Values.wrap(lhs == rhs);
        case NOT_EQUAL -> Values.wrap(lhs != rhs);
      };
    } else if (expression instanceof Ast.IntegerLiteral integer) {
      return Values.wrap(integer.value());
    } else if (expression instanceof Ast.Identifier e) {
      var bindingOpt = variableEnvironment.findBinding(e.name());
      return bindingOpt.get().get(e.name());
    } else if (expression instanceof Ast.Assignment e) {
      var bindingOpt = variableEnvironment.findBinding(e.name());
      var value = interpret(e.expression());
      if (bindingOpt.isPresent()) {
        bindingOpt.get().put(e.name(), value);
      } else {
        variableEnvironment.bindings().put(e.name(), value);
      }
      return value;
    } else if (expression instanceof Ast.IfExpression e) {
      var condition = interpret(e.condition()).asBool().value();
      if (condition) {
        return interpret(e.thenClause());
      } else {
        var elseClauseOpt = e.elseClause();
        // Optional で存在しなかったらnullを返す
        return elseClauseOpt.map(this::interpret).orElse(null);
      }
    } else if (expression instanceof Ast.WhileExpression e) {
      // 無限ループでひたすら評価
      while (true) {
        var condition = interpret(e.condition()).asBool().value(); // condition 部を評価

        // 条件が真であれば body を評価
        if (condition) {
          interpret(e.body());
        } else {
          // 条件が偽であれば脱出
          break;
        }
      }

      return Values.wrap(true);
    } else if (expression instanceof Ast.BlockExpression e) {
      Values.Value value = null; // 初期化
      for (var element : e.elements()) { // Block 内部を評価
        value = interpret(element);
      }
      return value; // 最後に評価した値を返す
    } else if (expression instanceof Ast.FunctionCall functionCall) {
      // 関数呼び出し
      var definition = functionEnvironment.get(functionCall.name());
      if (definition == null) {
        // 呼び出そうとした定義がない
        throw new RuntimeException("Function " + functionCall.name() + " is not found");
      }
      var actualParams = functionCall.args(); // Expression のList
      var formalParams = definition.args(); // String の List
      var body = definition.body();

      var values = actualParams.stream().map(this::interpret).toList(); // Expression なのでそれぞれ評価
      var backup = variableEnvironment; // 現在の環境を保持
      variableEnvironment = newEnvironment(Optional.of(variableEnvironment)); // 新たな環境を作って現在の環境をつなぐ

      int i = 0;
      for (var formalParamName : formalParams) {
        variableEnvironment.bindings().put(formalParamName, values.get(i));
        i++;
      }

      var result = interpret(body);
      variableEnvironment = backup; // 関数評価が終わったので戻す
      return result;

    } else {
      throw new RuntimeException("not reach here");
    }
  }

  public Values.Value callMain(Ast.Program program) {
    var topLevels = program.definitions();

    for (var topLevel : topLevels) {
      if (topLevel instanceof Ast.FunctionDefinition definition) {
        functionEnvironment.put(definition.name(), definition);
      } else if (topLevel instanceof Ast.GlobalVariableDefinition globalVariableDefinition) {
        variableEnvironment
            .bindings()
            .put(globalVariableDefinition.name(), interpret(globalVariableDefinition.expression()));
      }
    }

    var mainFunction = functionEnvironment.get("main");
    if (mainFunction != null) {
      return interpret(mainFunction.body());
    } else {
      throw new LanguageException("This program doesn't have main() function");
    }
  }
}
