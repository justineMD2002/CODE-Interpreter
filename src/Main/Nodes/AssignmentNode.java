package Main.Nodes;

public class AssignmentNode extends ASTNode {
    private final String variableName;
    private final ASTNode value;

    public AssignmentNode(String variableName, ASTNode value) {
        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableName() {
        return variableName;
    }

    public ASTNode getValue() {
        return value;
    }
}
