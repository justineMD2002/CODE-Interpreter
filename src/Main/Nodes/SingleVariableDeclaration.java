package Main.Nodes;
import java.util.*;

public class SingleVariableDeclaration extends ASTNode {
    private final String dataType;
    private final List<String> variableNames;

    public SingleVariableDeclaration(String dataType, List<String> variableNames) {
        this.dataType = dataType;
        this.variableNames = variableNames;
    }

    public String getDataType() {
        return dataType;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }
}
