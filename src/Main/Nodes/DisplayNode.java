package Main.Nodes;

public class DisplayNode extends ASTNode {
    private final String output;

    public DisplayNode(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }
}
