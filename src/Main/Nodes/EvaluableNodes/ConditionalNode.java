package Main.Nodes.EvaluableNodes;

import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.ExecutableCodeNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class ConditionalNode extends EvaluableNode {
    private final List<ASTNode> conditions;
    private final ExecutableCodeNode ifStatements;
    private final List<ExecutableCodeNode> elseIfBlocks;
    private final ExecutableCodeNode elseStatements;

    public ConditionalNode(List<ASTNode> conditions, ExecutableCodeNode ifStatements, List<ExecutableCodeNode> elseIfBlocks, ExecutableCodeNode elseStatements) {
        this.conditions = conditions;
        this.ifStatements = ifStatements;
        this.elseIfBlocks = elseIfBlocks;
        this.elseStatements = elseStatements;
    }

    public List<ASTNode> getConditions() {
        return conditions;
    }

    public ExecutableCodeNode getIfStatements() {
        return ifStatements;
    }

    public List<ExecutableCodeNode> getElseIfBlocks() {
        return elseIfBlocks;
    }

    public ExecutableCodeNode getElseStatements() {
        return elseStatements;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) {
        List<ASTNode> executables = null;
        for(int i=0; i<getConditions().size(); i++) {
            ASTNode condition = getConditions().get(i);
            if(condition instanceof ExpressionNode expressionNode) {
                LiteralNode result = expressionNode.evaluateExpression(symbolTable);
                if(result.getValue().equals("TRUE")) {
                    executables = (i == 0) ? getIfStatements().getStatements() : getElseIfBlocks().get(i-1).getStatements();
                    break;
                }
            }
        }
        if(executables == null) {
            executables = getElseStatements().getStatements();
        }

        for(ASTNode executable : executables) {
            if(executable instanceof EvaluableNode evaluableNode) {
                evaluableNode.evaluate(symbolTable);
            }
        }

    }
}
