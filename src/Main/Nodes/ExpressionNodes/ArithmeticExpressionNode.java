package Main.Nodes.ExpressionNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.SymbolTable;
import Main.Token.Token;

public class ArithmeticExpressionNode extends ExpressionNode {
    private final ASTNode rightOperand;
    private final ASTNode leftOperand;

    private final Token.Type operator;

    public ArithmeticExpressionNode(ASTNode leftOperand, Token.Type operator, ASTNode rightOperand, int lineNumber) {
        super(lineNumber);
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

    public LiteralNode evaluateExpression(SymbolTable symbolTable) throws VariableDeclarationException {
        LiteralNode leftValueNode = evaluate(getLeftOperand(), symbolTable);
        LiteralNode rightValueNode = evaluate(getRightOperand(), symbolTable);

        Object leftValue = leftValueNode.getValue();
        Object rightValue = rightValueNode.getValue();

        if (leftValue instanceof Integer && rightValue instanceof Integer) {
            int intResult = evaluateIntExpression((int) leftValue, (int) rightValue);
            return new LiteralNode(intResult);
        } else if (leftValue instanceof Float || rightValue instanceof Float) {
            float doubleResult = evaluateDoubleExpression(leftValue, rightValue);
            return new LiteralNode(doubleResult);
        } else {
            String leftSimpleName = leftValue.getClass().getSimpleName().equals("String") ? "BOOL" : leftValue.getClass().getSimpleName();
            String rightSimpleName = rightValue.getClass().getSimpleName().equals("String") ? "BOOL" : rightValue.getClass().getSimpleName();
            throw new IllegalArgumentException("ERROR: Unsupported operand types: " + leftSimpleName + " and " + rightSimpleName + ". at line " + (getLineNumber()+1));
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
                    throw new ArithmeticException("ERROR: Division by zero. at line " + (getLineNumber()+1));
                }
                return leftValue / rightValue;
            }
            case Modulo -> {
                if (rightValue == 0) {
                    throw new ArithmeticException("ERROR: Division by zero");
                }
                return leftValue % rightValue;
            }
            // Handle other operators if needed
            default -> throw new IllegalArgumentException("ERROR: Unsupported operator: " + getOperator() + ". at line " + (getLineNumber()+1));
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
                    throw new ArithmeticException("ERROR: Division by zero. at line " + (getLineNumber()+1) );
                }
                return leftDouble / rightDouble;
            }
            case Modulo -> {
                if (rightDouble == 0.0) {
                    throw new ArithmeticException("ERROR: Division by zero. at line " + (getLineNumber()+1) );
                }
                return leftDouble % rightDouble;
            }
            // Handle other operators if needed
            default -> throw new IllegalArgumentException("ERROR: Unsupported operator: " + getOperator() + ". at line " + (getLineNumber()+1));
        }
    }

}
