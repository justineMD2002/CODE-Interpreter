package Main.Nodes.ExpressionNodes;

import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.EvaluableNodes.VariableNode;
import Main.Nodes.SymbolTable;

public abstract class ExpressionNode extends ASTNode {
    abstract public LiteralNode evaluateExpression(SymbolTable symbolTable);

    protected LiteralNode evaluate(ASTNode node, SymbolTable symbolTable) {
        return switch (node) {
            case LiteralNode literalNode -> literalNode;
            case VariableNode variableNode -> {
                LiteralNode value = symbolTable.getValue(variableNode.getVariableName());
                if(value != null) {
                    yield value;
                } else if((int)variableNode.getInitialValue() == -1) {
                    if(variableNode.getInitialValue() instanceof Integer) {
                        yield new LiteralNode((int) value.getValue()*-1 );
                    } else if(variableNode.getInitialValue() instanceof Double) {
                        yield new LiteralNode((double) value.getValue()*-1.0);
                    }
                }
                throw new ArithmeticException("ERROR: Expression value analyzed returned null possibly because of null operands in the expression.");
            }
            case ExpressionNode expressionNode -> expressionNode.evaluateExpression(symbolTable);
            case null, default ->
                    throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getSimpleName());
        };
    }
}
