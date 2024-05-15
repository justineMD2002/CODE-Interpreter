package Main.Nodes.ExpressionNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.EvaluableNodes.VariableNode;
import Main.Nodes.SymbolTable;

public abstract class ExpressionNode extends ASTNode {
    private final int lineNumber;

    public ExpressionNode(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    abstract public LiteralNode evaluateExpression(SymbolTable symbolTable) throws VariableDeclarationException;


    public int getLineNumber() {
        return lineNumber;
    }

    protected LiteralNode evaluate(ASTNode node, SymbolTable symbolTable) throws VariableDeclarationException {
        return switch (node) {
            case LiteralNode literalNode -> literalNode;
            case VariableNode variableNode -> {
                String variableName = variableNode.getVariableName();
                if (!symbolTable.getInitializedVariables().containsKey(variableName)) {
                    throw new VariableDeclarationException("ERROR: Variable '" + variableName + "' not declared.", getLineNumber());
                } else if(symbolTable.getValue(variableName) != null && symbolTable.getValue(variableName).getValue() != null) {
                     if (variableNode.getInitialValue() == null) {
                        yield symbolTable.getValue(variableName);
                    } else if ((int) variableNode.getInitialValue() == -1) {
                        LiteralNode value = symbolTable.getValue(variableName);
                        if (variableNode.getInitialValue() instanceof Integer) {
                            yield new LiteralNode((int) value.getValue() * -1);
                        } else if (variableNode.getInitialValue() instanceof Double) {
                            yield new LiteralNode((double) value.getValue() * -1.0);
                        }
                    }
                }
                throw new ArithmeticException("ERROR: Expression value analyzed returned null possibly because of null operands in the expression. at line " + (getLineNumber()+1));
            }
            case ExpressionNode expressionNode -> expressionNode.evaluateExpression(symbolTable);
            case null, default -> {
                assert node != null;
                throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getSimpleName() + ". at line " + (getLineNumber()+1));
            }
        };
    }
}
