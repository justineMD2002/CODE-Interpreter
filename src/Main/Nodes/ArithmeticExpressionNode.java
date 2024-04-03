package Main.Nodes;

import Main.Token.Token;

public class ArithmeticExpressionNode extends ASTNode {
    private final ASTNode rightOperand;
    private final ASTNode leftOperand;

    private final Token.Type operator;

    public ArithmeticExpressionNode(ASTNode leftOperand, Token.Type operator, ASTNode rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    public ASTNode getRightOperand() {
        return rightOperand;
    }

    public ASTNode getLeftOperand() {
        return leftOperand;
    }

    public Token.Type getOperator() {
        return operator;
    }

    public LiteralNode evaluateExpression() {
        int leftValue = evaluate(getLeftOperand());
        int rightValue = evaluate(getRightOperand());

        int result = switch (getOperator()) {
            case Plus -> leftValue + rightValue;
            case Minus -> leftValue - rightValue;
            case Times -> leftValue * rightValue;
            case Divide -> {
                if (rightValue == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                yield leftValue / rightValue;
            }
            // Handle other operators if needed
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };

        return new LiteralNode(result);
    }

    private int evaluate(ASTNode expression) {
        if (expression instanceof LiteralNode) {
            return Integer.parseInt(((LiteralNode) expression).getValue().toString());
        } else if (expression instanceof ArithmeticExpressionNode arithmeticExpression) {
            return Integer.parseInt(evaluateExpression().getValue().toString());
        }
        throw new IllegalArgumentException("Invalid expression node: " + expression);
    }
}
