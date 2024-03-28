package Main.Nodes;
import java.util.*;

public class VariableDeclarationsNode extends ASTNode {
    private final List<SingleVariableDeclaration> variableDeclarations;

    public VariableDeclarationsNode(List<SingleVariableDeclaration> variableDeclarations) {
        this.variableDeclarations = variableDeclarations;
    }

    public List<SingleVariableDeclaration> getVariableDeclarations() {
        return variableDeclarations;
    }

}


