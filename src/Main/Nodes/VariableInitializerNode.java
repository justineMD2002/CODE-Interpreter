package Main.Nodes;

import java.util.HashMap;
import java.util.Map;

public class VariableInitializerNode extends ASTNode {
    private final Map<String, LiteralNode> initializedVariables;

    public VariableInitializerNode() {
        this.initializedVariables = new HashMap<>();
    }

    public Map<String, LiteralNode> getInitializedVariables() {
        return initializedVariables;
    }

    public void setValue(String variableName, LiteralNode value) {
        initializedVariables.put(variableName, value);
    }

    public LiteralNode getValue(String variableName) {
        return initializedVariables.get(variableName);
    }
}
