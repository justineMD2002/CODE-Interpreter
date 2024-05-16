package Main.Nodes.EvaluableNodes;

import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.SymbolTable;

public class VariableNode extends EvaluableNode  {

    private final String variableName;
    private final Object initialValue;

    public VariableNode(String variableName, Object initialValue) {
        this.variableName = variableName;
        this.initialValue = initialValue;
    }
    @Override
    public void evaluate(SymbolTable symbolTable) {
        LiteralNode initialValueNode = new LiteralNode(initialValue);
        symbolTable.setValue(variableName, initialValueNode);
    }

    public String getVariableName() {
        return variableName;
    }

    public Object getInitialValue() {
        return initialValue;
    }
}
