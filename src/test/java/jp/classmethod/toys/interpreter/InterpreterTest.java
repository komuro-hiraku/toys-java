/* Copyright (C) 2023 komuro-hiraku */
package jp.classmethod.toys.interpreter;

import static jp.classmethod.toys.interpreter.Ast.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InterpreterTest {

  private final Interpreter interpreter = new Interpreter();

  @Test
  public void test10Plus20ShouldWork() {
    Ast.Expression e = add(integer(10), integer(20));
    assertEquals(30, interpreter.interpret(e).asInt().value());
  }

  @Test
  public void testFactorial() {
    List<Ast.TopLevel> topLevels =
        List.of(
            // define main() {
            //    fact(5);
            // }
            Ast.DefineFunction("main", List.of(), Block(Ast.call("fact", integer(5)))),
            // define fact(n) {
            //  if (n < 2) {
            //    1;
            //  } else {
            //    n + fact(n - 1);
            //  }
            // }
            DefineFunction(
                "fact",
                List.of("n"),
                Block(
                    If(
                        Ast.lessThan(Ast.symbol("n"), integer(2)),
                        integer(1),
                        Optional.of(
                            multiply(
                                symbol("n"), call("fact", subtract(symbol("n"), integer(1)))))))));
    var result = interpreter.callMain(new Ast.Program(topLevels));
    assertEquals(120, result.asInt().value());
  }
}
