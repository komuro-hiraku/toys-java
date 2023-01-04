package jp.classmethod.toys.interpreter;

import org.junit.jupiter.api.Test;

import static jp.classmethod.toys.interpreter.Ast.add;
import static jp.classmethod.toys.interpreter.Ast.integer;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InterpreterTest {

    private final Interpreter interpreter = new Interpreter();

    @Test
    public void test10Plus20ShouldWork() {
        Ast.Expression e = add(
                integer(10), integer(20)
        );
        assertEquals(
                30, interpreter.interpret(e)
        );
    }

}