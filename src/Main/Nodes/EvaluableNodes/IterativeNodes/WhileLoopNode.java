package Main.Nodes.EvaluableNodes.IterativeNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class WhileLoopNode extends IterativeNode {
    public WhileLoopNode(ASTNode condition, List<ASTNode> whileStatements) {
        super(condition, whileStatements);
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableInitializationException, BreakException, VariableDeclarationException {
        ASTNode condition = getCondition();
        evaluateLoop(symbolTable, condition);
    }
}
