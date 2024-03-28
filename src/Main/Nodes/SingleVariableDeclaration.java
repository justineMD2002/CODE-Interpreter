package Main.Nodes;
import java.util.*;

public class SingleVariableDeclaration extends ASTNode {
    private final String dataType;
    private final Map<String, Object> variables;

    public SingleVariableDeclaration(String dataType, List<String> variableNames) {
        this.dataType = dataType;
        this.variables = new HashMap<>();
        for (String variableName : variableNames) {
            this.variables.put(variableName, null);
        }
    }

    public String getDataType() {
        return dataType;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setValue(String variableName, Object value) {
        variables.put(variableName, value);
    }

    public Object getValue(String variableName) {
        return variables.get(variableName);
    }
}
