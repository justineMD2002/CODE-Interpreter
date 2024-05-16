package Main.Nodes.EvaluableNodes.IterativeNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.ScannedInputException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class WhileLoopNode extends IterativeNode {
    public WhileLoopNode(ASTNode condition, List<ASTNode> whileStatements, int lineNumber) {
        super(condition, whileStatements, lineNumber);
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableInitializationException, BreakException, VariableDeclarationException, ScannedInputException {
        ASTNode condition = getCondition();
        evaluateLoop(symbolTable, condition);
    }
}
