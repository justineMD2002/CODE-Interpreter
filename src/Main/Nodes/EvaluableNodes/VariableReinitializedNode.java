package Main.Nodes.EvaluableNodes;

import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class VariableReinitializedNode extends EvaluableNode {
    private final List<String> variableNames;
    private LiteralNode value;

    public VariableReinitializedNode(List<String> variableNames, LiteralNode value) {
        this.variableNames = variableNames;
        this.value = value;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public LiteralNode getValue() {
        return value;
    }

    public void setValue(LiteralNode value) {
        this.value = value;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) {
        for(String varName : getVariableNames()) {
            if(getValue().getValue() instanceof ExpressionNode expressionNode) {
                setValue(expressionNode.evaluateExpression(symbolTable));
            } else if(getValue().getValue() instanceof VariableNode variableNode) {
                if(variableNode.getInitialValue() == null) {
                    setValue(symbolTable.getValue(variableNode.getVariableName()));
                } else if((int)variableNode.getInitialValue() == -1) {
                    if(variableNode.getInitialValue() instanceof Integer) {
                        setValue(new LiteralNode((int) symbolTable.getValue(variableNode.getVariableName()).getValue()*-1));
                    } else if(variableNode.getInitialValue() instanceof Double) {
                        setValue(new LiteralNode((double) symbolTable.getValue(variableNode.getVariableName()).getValue()*-1.0));
                    }
                }
            }
            symbolTable.setValue(varName, getValue());
        }
    }

}
