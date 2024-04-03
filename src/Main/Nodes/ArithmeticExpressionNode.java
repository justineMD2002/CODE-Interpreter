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
        LiteralNode leftValueNode = evaluate(getLeftOperand());
        LiteralNode rightValueNode = evaluate(getRightOperand());

        int leftValue = Integer.parseInt(leftValueNode.getValue().toString());
        int rightValue = Integer.parseInt(rightValueNode.getValue().toString());

        System.out.println(leftValue + " " + getOperator() + " " + rightValue);

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

    private LiteralNode evaluate(ASTNode expression) {
        if (expression instanceof LiteralNode) {
            return (LiteralNode) expression;
        } else if (expression instanceof ArithmeticExpressionNode arithmeticExpression) {
            return arithmeticExpression.evaluateExpression();
        }
        throw new IllegalArgumentException("Invalid expression node: " + expression);
    }
}
