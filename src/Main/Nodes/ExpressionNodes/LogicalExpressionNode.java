package Main.Nodes.ExpressionNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.SymbolTable;
import Main.Token.Token;

import java.util.Objects;

public class LogicalExpressionNode extends ExpressionNode {
    private final ASTNode rightOperand;
    private final ASTNode leftOperand;

    private final Token.Type operator;

    public LogicalExpressionNode(ASTNode leftOperand, Token.Type operator, ASTNode rightOperand) {
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

    private boolean evaluateBooleanExpression(boolean leftValue, boolean rightValue) {
        return switch (getOperator()) {
            case And -> leftValue && rightValue;
            case Or -> leftValue || rightValue;
            default -> throw new IllegalArgumentException("ERROR: Unsupported operator: " + getOperator());
        };
    }

    private boolean evaluateBooleanExpression(boolean operand) {
        if (Objects.requireNonNull(getOperator()) == Token.Type.Not) {
            return !operand;
        }
        throw new IllegalArgumentException("ERROR: Unsupported operator: " + getOperator());
    }

    @Override
    public LiteralNode evaluateExpression(SymbolTable symbolTable) throws VariableDeclarationException {
        LiteralNode leftValueNode = evaluate(getLeftOperand(), symbolTable);
        Object leftValue = leftValueNode.getValue();

        if(leftValue instanceof String) {
            leftValue = leftValue.equals("TRUE");
        }

        if (getRightOperand() != null) {
            LiteralNode rightValueNode = evaluate(getRightOperand(), symbolTable);
            Object rightValue = rightValueNode.getValue();

            if(rightValue instanceof String) {
                rightValue = rightValue.equals("TRUE");
            }

            if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
                boolean boolResult = evaluateBooleanExpression((boolean) leftValue, (boolean) rightValue);
                return boolResult ? new LiteralNode("TRUE") : new LiteralNode("FALSE");
            } else {
                String leftSimpleName = leftValue.getClass().getSimpleName().equals("String") ? "BOOL" : leftValue.getClass().getSimpleName();
                String rightSimpleName = rightValue.getClass().getSimpleName().equals("String") ? "BOOL" : rightValue.getClass().getSimpleName();
                throw new IllegalArgumentException("ERROR: Unsupported operand types: " + leftSimpleName + " and " + rightSimpleName);
            }
        } else {
            if (leftValue instanceof Boolean) {
                boolean boolResult = evaluateBooleanExpression((Boolean) leftValue);
                return boolResult ? new LiteralNode("TRUE") : new LiteralNode("FALSE");
            } else {
                throw new IllegalArgumentException("ERROR: Unary operation can only be applied to boolean types.");
            }
        }
    }
}
