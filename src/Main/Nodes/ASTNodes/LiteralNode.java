package Main.Nodes.ASTNodes;

public class LiteralNode extends ASTNode {
    private final Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
