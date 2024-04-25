package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.DisplayException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class DisplayNode extends EvaluableNode {
    private final List<ASTNode> expressions;

    public DisplayNode(List<ASTNode> expressions) {
        this.expressions = expressions;
    }

    public List<ASTNode> getExpressions() {
        return expressions;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) {

        StringBuilder output = new StringBuilder();
        for (ASTNode expression : expressions) {
            if(expression instanceof ExpressionNode expressionNode) {
                output.append(expressionNode.evaluateExpression(symbolTable).getValue());
            } else if(expression instanceof VariableNode variableNode) {
                if(variableNode.getInitialValue() == null) {
                    output.append(symbolTable.getValue(((VariableNode) expression).getVariableName()).getValue());
                }
            } else if(expression instanceof LiteralNode literalNode) {
                output.append(literalNode.getValue());
            }
        }
        System.out.print(output);
    }
}
