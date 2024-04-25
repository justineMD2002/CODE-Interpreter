package Main.Nodes;

import Main.Nodes.ASTNodes.LiteralNode;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, LiteralNode> initializedVariables;

    public SymbolTable() {
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
    public void clear() {
        initializedVariables.clear();
    }
}
