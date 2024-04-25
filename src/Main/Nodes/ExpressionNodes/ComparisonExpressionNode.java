package Main.Nodes.ExpressionNodes;

import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.EvaluableNodes.VariableNode;
import Main.Nodes.SymbolTable;
import Main.Token.Token;

public class ComparisonExpressionNode extends ExpressionNode {
    private final ASTNode rightOperand;
    private final ASTNode leftOperand;

    private final Token.Type operator;

    public ComparisonExpressionNode(ASTNode leftOperand, Token.Type operator, ASTNode rightOperand) {
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

    @Override
    public LiteralNode evaluateExpression(SymbolTable symbolTable) {
        LiteralNode leftValueNode = evaluate(getLeftOperand(), symbolTable);
        LiteralNode rightValueNode = evaluate(getRightOperand(), symbolTable);

        Object leftValue = leftValueNode.getValue();
        Object rightValue = rightValueNode.getValue();

        if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftNum = ((Number) leftValue).doubleValue();
            double rightNum = ((Number) rightValue).doubleValue();

            boolean comparisonResult = switch (getOperator()) {
                case Less -> leftNum < rightNum;
                case LessEqual -> leftNum <= rightNum;
                case Greater -> leftNum > rightNum;
                case GreaterEqual -> leftNum >= rightNum;
                case Equals -> leftNum == rightNum;
                case NotEqual -> leftNum != rightNum;
                default -> throw new IllegalArgumentException("ERROR: Unsupported operator: " + getOperator());
            };

            return comparisonResult ? new LiteralNode("TRUE") : new LiteralNode("FALSE");
        } else {
            throw new IllegalArgumentException("ERROR: Comparison operation can only be applied to numeric types.");
        }
    }
}
