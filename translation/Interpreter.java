package translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import translation.Expr.Symbol;
import translation.Stmt.PrintOnly;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment(); // Global environment
    private Environment environment = globals; // Current environment
    private final BufferedReader reader;

    Interpreter() {
        reader = new BufferedReader(new InputStreamReader(System.in));
        // Add built-in functions to the global environment.
        globals.define("floor", new FloorFun());
        globals.define("substring", new SubstringFunction());
        globals.define("clock", new ClockFunction());
    }

    private static final int[] RANDOM_SEQUENCE = { 57, 97, 28, 7, 71, 1, 79, 83, 64, 82, 89, 24 };
    private int randIndex = 0;

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            default:
                return null; // Unreachable
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
            case MINUS:
            case SLASH:
            case STAR:
                return evaluateArithmetic(expr.operator, left, right);
            case PLUS:
                return evaluatePlus(expr.operator, left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            default:
                return null; // Unreachable
        }
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitDynamicLiteralExpr(Expr.DynamicLiteral expr) {
        switch (expr.name.type) {
            case READ:
                return handleRead(); // Handle `read` literals.
            case RAND:
                return handleRand(); // Handle `rand` literals.
            default:
                throw new RuntimeError(expr.name, "Unexpected dynamic literal type.");
        }
    }

    private String handleRead() {
        System.out.print("input required > ");
        try {
            String input = reader.readLine();
            if (input != null) {
                return input.trim(); // Return the trimmed input.
            } else {
                System.out.println("Debug: No more input available (EOF). Returning empty string.");
                return "";
            }
        } catch (IOException e) {
            throw new RuntimeError(new Token(TokenType.READ, "<-", null, 1), "Error reading input.");
        }
    }

    private double handleRand() {
        // Return the next "random" number in the sequence.
        double randomValue = RANDOM_SEQUENCE[randIndex];
        randIndex = (randIndex + 1) % RANDOM_SEQUENCE.length; // Cycle through the list.
        return randomValue;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitStringLoopStmt(Stmt.StringLoop stmt) {
        Object iterable = evaluate(stmt.iterable);

        if (!(iterable instanceof String)) {
            throw new RuntimeError(stmt.name, "String loop can only iterate over strings.");
        }

        String str = (String) iterable;

        for (int i = 0; i < str.length(); i++) {
            environment = new Environment(environment);
            environment.define(stmt.name.lexeme, String.valueOf(str.charAt(i)));

            execute(stmt.body);

            environment = environment.enclosing; // Restore the previous environment.
        }

        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        execute(stmt.initializer);
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
            evaluate(stmt.increment);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value); // Use the `Return` class to exit the function.
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private Object evaluatePlus(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return (double) left + (double) right;
        }
        if (left instanceof String && right instanceof String) {
            return (String) left + (String) right;
        }
        throw new RuntimeError(operator, "Operands must be two numbers or two strings.");
    }

    private Object evaluateArithmetic(Token operator, Object left, Object right) {
        double leftNum = (double) left;
        double rightNum = (double) right;

        switch (operator.type) {
            case GREATER:
                return leftNum > rightNum;
            case GREATER_EQUAL:
                return leftNum >= rightNum;
            case LESS:
                return leftNum < rightNum;
            case LESS_EQUAL:
                return leftNum <= rightNum;
            case MINUS:
                return leftNum - rightNum;
            case SLASH:
                return leftNum / rightNum;
            case STAR:
                return leftNum * rightNum;
            default:
                return null; // Unreachable
        }
    }

    @Override
    public Object visitSymbolExpr(Symbol expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSymbolExpr'");
    }

    @Override
    public Void visitPrintOnlyStmt(PrintOnly stmt) {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }
}
