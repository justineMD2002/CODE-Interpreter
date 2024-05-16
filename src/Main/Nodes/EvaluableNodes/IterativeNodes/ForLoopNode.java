package Main.Nodes.EvaluableNodes.IterativeNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.ScannedInputException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class ForLoopNode extends IterativeNode {
    private final ASTNode initialization;
    public ForLoopNode(ASTNode initialization, ASTNode condition, List<ASTNode> forStatements, int lineNumber) {
        super(condition, forStatements, lineNumber);
        this.initialization = initialization;
    }

    public ASTNode getInitialization() {
        return initialization;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableInitializationException, BreakException, VariableDeclarationException, ScannedInputException {
        ASTNode condition = getCondition();
        if(getInitialization() instanceof EvaluableNode evaluableNode) {
            evaluableNode.evaluate(symbolTable);
        }
        evaluateLoop(symbolTable, condition);
    }
}
