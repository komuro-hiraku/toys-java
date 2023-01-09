/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Interpreter */
public class Interpreter {

  // TODO: Ast.Environment の実装がない
  // private final Ast.Environment variableEnvironment;

  public final Map<String, Integer> environment;

  private final Map<String, Ast.FunctionDefinition> functionEnvironment;

  public Interpreter() {
    this.functionEnvironment = new HashMap<>();
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
        int condition = interpret(e.condition()); // condition 部を評価

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
      int value = 0; // 初期化
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

  public int callMain(Ast.Program program) {
    var topLevels = program.definitions();

    for (var topLevel : topLevels) {
      if (topLevel instanceof Ast.FunctionDefinition definition) {
        functionEnvironment.put(definition.name(), definition);
      } else {
        // TODO: Global Variables
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
