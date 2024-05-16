package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class DisplayNode extends EvaluableNode {
    private final List<ASTNode> expressions;
    private final int lineNumber;

    public DisplayNode(List<ASTNode> expressions, int lineNumber) {
        this.expressions = expressions;
        this.lineNumber = lineNumber;
    }

    public List<ASTNode> getExpressions() {
        return expressions;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableInitializationException, VariableDeclarationException {
        StringBuilder output = new StringBuilder();
        LiteralNode value;
        for (ASTNode expression : getExpressions()) {
            if(expression instanceof ExpressionNode expressionNode) {
                output.append(expressionNode.evaluateExpression(symbolTable).getValue());
            } else if(expression instanceof VariableNode variableNode) {
                if(!symbolTable.getInitializedVariables().containsKey(variableNode.getVariableName())) {
                    throw new VariableDeclarationException("ERROR: Variable " + variableNode.getVariableName() + " is not declared.", getLineNumber());
                }
                value = symbolTable.getValue(((VariableNode) expression).getVariableName());
                if(value == null || value.getValue() == null) {
                    throw new VariableInitializationException("ERROR: Variable " + ((VariableNode) expression).getVariableName() + " is not initialized.",getLineNumber());
                } else if(variableNode.getInitialValue() == null) {
                    output.append(value.getValue());
                } else if((int)variableNode.getInitialValue() == -1) {
                    if(variableNode.getInitialValue() instanceof Integer) {
                        output.append((int) value.getValue()*-1);
                    } else if(variableNode.getInitialValue() instanceof Double) {
                        output.append((double) value.getValue()*-1.0);
                    } else {
                        throw new VariableInitializationException("ERROR: Value of type " + value.getValue() + " cannot be negated.", getLineNumber());
                    }
                }
            } else if(expression instanceof LiteralNode literalNode) {
                output.append(literalNode.getValue());
            }
        }
        System.out.print(output);
    }
}
