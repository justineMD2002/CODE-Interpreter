package Main.Nodes.ExpressionNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.SymbolTable;
import Main.Token.Token;

public class ComparisonExpressionNode extends ExpressionNode {
    private final ASTNode rightOperand;
    private final ASTNode leftOperand;

    private final Token.Type operator;

    public ComparisonExpressionNode(ASTNode leftOperand, Token.Type operator, ASTNode rightOperand, int lineNumber) {
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

    @Override
    public LiteralNode evaluateExpression(SymbolTable symbolTable) throws VariableDeclarationException {
        LiteralNode leftValueNode = evaluate(getLeftOperand(), symbolTable);
        LiteralNode rightValueNode = evaluate(getRightOperand(), symbolTable);

        Object leftValue = leftValueNode.getValue();
        Object rightValue = rightValueNode.getValue();

        if (leftValue instanceof Number && rightValue instanceof Number) {
            return compareNumbers((Number) leftValue, (Number) rightValue);
        } else if(leftValue instanceof Character && rightValue instanceof Character) {
            return compareCharacters((Character) leftValue, (Character) rightValue);
        } else if(isBooleanString(leftValue) && isBooleanString(rightValue)) {
            return compareBooleans(Boolean.parseBoolean(leftValue.toString()), Boolean.parseBoolean(rightValue.toString()));
        } else {
            throw new IllegalArgumentException("ERROR: Comparison operation can only be applied to same data types. at line " + (getLineNumber()+1));
        }
    }

    private LiteralNode compareNumbers(Number leftNum, Number rightNum) {
        boolean comparisonResult = switch (getOperator()) {
            case Less -> leftNum.doubleValue() < rightNum.doubleValue();
            case LessEqual -> leftNum.doubleValue() <= rightNum.doubleValue();
            case Greater -> leftNum.doubleValue() > rightNum.doubleValue();
            case GreaterEqual -> leftNum.doubleValue() >= rightNum.doubleValue();
            case Equals -> leftNum.doubleValue() == rightNum.doubleValue();
            case NotEqual -> leftNum.doubleValue() != rightNum.doubleValue();
            default -> throw new IllegalArgumentException("ERROR: Unsupported operator: " + getOperator() + ". at line " + (getLineNumber()+1));
        };
        return new LiteralNode(comparisonResult ? "TRUE" : "FALSE");
    }

    private LiteralNode compareCharacters(Character leftChar, Character rightChar) {
        boolean comparisonResult = switch (getOperator()) {
            case Equals -> leftChar == rightChar;
            case NotEqual -> leftChar != rightChar;
            default -> throw new IllegalArgumentException("ERROR: Cannot apply operation type to value of type Character. at line " + (getLineNumber()+1));
        };
        return new LiteralNode(comparisonResult ? "TRUE" : "FALSE");
    }

    private LiteralNode compareBooleans(boolean leftBool, boolean rightBool) {
        boolean comparisonResult = switch (getOperator()) {
            case Equals -> leftBool == rightBool;
            case NotEqual -> leftBool != rightBool;
            default -> throw new IllegalArgumentException("ERROR: Cannot apply operation type to value of type BOOL. at line " + (getLineNumber()+1));
        };
        return new LiteralNode(comparisonResult ? "TRUE" : "FALSE");
    }

    private boolean isBooleanString(Object value) {
        return value instanceof String && (value.equals("TRUE") || value.equals("FALSE"));
    }
}
