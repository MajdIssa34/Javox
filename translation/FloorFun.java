package translation;

import java.time.Instant;
import java.util.List;

public class FloorFun implements LoxCallable {
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // making sure there is a number to be transformed
        if (!(arguments.get(0) instanceof Double)) {
            throw new RuntimeError(null, "floor() requires a number argument.");
        }
        return Math.floor((Double) arguments.get(0));
    }

    @Override
    public int arity() {
        return 1; // `floor` accepts 1 argument.
    }

    @Override
    public String toString() {
        return "<native fn floor>";
    }
}

class SubstringFunction implements LoxCallable {
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        if (arguments.size() != 3) {
            // Ensure a valid token is passed here.
            throw new RuntimeError(new Token(TokenType.IDENTIFIER, "substring", null, 1),
                    "Expected 3 arguments but got " + arguments.size() + ".");
        }

        if (!(arguments.get(0) instanceof String)) {
            throw new RuntimeError(new Token(TokenType.IDENTIFIER, "substring", null, 1),
                    "First argument must be a string.");
        }

        String str = (String) arguments.get(0);
        double startIndex = (double) arguments.get(1);
        double endIndex = (double) arguments.get(2);

        if (startIndex < 0 || endIndex > str.length()) {
            throw new RuntimeError(new Token(TokenType.IDENTIFIER, "substring", null, 1),
                    "substring error: invalid indices.");
        }
        if (endIndex <= startIndex) {
            return "";
        }
        return str.substring((int) startIndex, (int) endIndex);
    }

    @Override
    public int arity() {
        return 3;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}

// Define a built-in function class for `clock`.
class ClockFunction implements LoxCallable {
    @Override
    public int arity() {
        return 0; // `clock` takes no arguments.
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) Instant.now().toEpochMilli() / 1000.0; // Return the current time in seconds.
    }

    @Override
    public String toString() {
        return "<native fn clock>";
    }
}
