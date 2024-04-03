package Main.Nodes;

import java.util.*;

public class ExecutableCodeNode extends ASTNode {
    private List<ASTNode> statements;

    public ExecutableCodeNode(List<ASTNode> statements) {
        setStatements(statements);
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    public void setStatements(List<ASTNode> statements) {
        this.statements = statements;
    }
}
