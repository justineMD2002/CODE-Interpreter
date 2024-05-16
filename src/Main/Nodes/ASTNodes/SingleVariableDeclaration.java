package Main.Nodes.ASTNodes;
import Main.Nodes.EvaluableNodes.VariableNode;

import java.util.*;

public class SingleVariableDeclaration extends ASTNode {
    private final String dataType;
    private final List<VariableNode> variableNames;

    public SingleVariableDeclaration(String dataType, List<VariableNode> variableNames) {
        this.dataType = dataType;
        this.variableNames = variableNames;
    }

    public String getDataType() {
        return dataType;
    }

    public List<VariableNode> getVariableNames() {
        return variableNames;
    }
}
