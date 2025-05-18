package translation;

import java.util.List;

interface LoxCallable {
    int arity(); // Number of parameters
    Object call(Interpreter interpreter, List<Object> arguments); // Function call logic
}

