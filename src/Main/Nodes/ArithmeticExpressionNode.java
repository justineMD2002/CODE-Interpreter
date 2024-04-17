package Main.Nodes;

import Main.Token.Token;

public class ArithmeticExpressionNode extends ExpressionNode {
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

        Object leftValue = leftValueNode.getValue();
        Object rightValue = rightValueNode.getValue();

        if (leftValue instanceof Integer && rightValue instanceof Integer) {
            int intResult = evaluateIntExpression((int) leftValue, (int) rightValue);
            return new LiteralNode(intResult);
        } else if (leftValue instanceof Float || rightValue instanceof Float) {
            float doubleResult = evaluateDoubleExpression(leftValue, rightValue);
            return new LiteralNode(doubleResult);
        } else {
            throw new IllegalArgumentException("Unsupported operand types: " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName());
        }
    }

    private int evaluateIntExpression(int leftValue, int rightValue) {
        switch (getOperator()) {
            case Plus -> {
                return leftValue + rightValue;
            }
            case Minus -> {
                return leftValue - rightValue;
            }
            case Times -> {
                return leftValue * rightValue;
            }
            case Divide -> {
                if (rightValue == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return leftValue / rightValue;
            }
            case Modulo -> {
                if (rightValue == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return leftValue % rightValue;
            }
            // Handle other operators if needed
            default -> throw new IllegalArgumentException("Unsupported operator: " + getOperator());
        }
    }

    private float evaluateDoubleExpression(Object leftValue, Object rightValue) {
        float leftDouble = leftValue instanceof Float ? (float) leftValue : (int) leftValue;
        float rightDouble = rightValue instanceof Float ? (float) rightValue : (int) rightValue;

        switch (getOperator()) {
            case Plus -> {
                return leftDouble + rightDouble;
            }
            case Minus -> {
                return leftDouble - rightDouble;
            }
            case Times -> {
                return leftDouble * rightDouble;
            }
            case Divide -> {
                if (rightDouble == 0.0) {
                    throw new ArithmeticException("Division by zero");
                }
                return leftDouble / rightDouble;
            }
            case Modulo -> {
                if (rightDouble == 0.0) {
                    throw new ArithmeticException("Division by zero");
                }
                return leftDouble % rightDouble;
            }
            // Handle other operators if needed
            default -> throw new IllegalArgumentException("Unsupported operator: " + getOperator());
        }
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
