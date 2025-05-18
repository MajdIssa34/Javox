package translation;

import translation.Expr.Assign;
import translation.Expr.Call;
import translation.Expr.DynamicLiteral;
import translation.Expr.Logical;
import translation.Expr.Variable;

public class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme,
        expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null)
      return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    // Special handling for '!!' (rand) and '<-' (read)
    if (expr.operator.type == TokenType.RAND) {
      return "(rand)"; // Pretty-print '!!' as 'rand'
    } else if (expr.operator.type == TokenType.READ) {
      return "(read)"; // Pretty-print '<-' as 'read'
    } else {
      return parenthesize(expr.operator.lexeme, expr.right);
    }
  }

  // add the symbol functoin
  public String visitSymbolExpr(Expr.Symbol expr) {
    return ":" + expr.value;
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Literal(45.67)));

    System.out.println(new AstPrinter().print(expression));
  }

  @Override
  public String visitDynamicLiteralExpr(DynamicLiteral expr) {
    return expr.name.type.name();
    // Below is the actual implementation of read.
    // System.out.print("Enter value: ");
    // Scanner scanner = new Scanner(System.in);
    // String input = scanner.nextLine();
    // scanner.close();
    // return input;
  }

  @Override
  public String visitVariableExpr(Variable expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitVariableExpr'");
  }

  @Override
  public String visitAssignExpr(Assign expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
  }

  @Override
  public String visitCallExpr(Call expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
  }

  @Override
  public String visitLogicalExpr(Logical expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
  }
}
