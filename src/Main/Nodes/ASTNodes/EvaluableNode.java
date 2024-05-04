package Main.Nodes.ASTNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.SymbolTable;

public abstract class EvaluableNode extends ASTNode {
    public abstract void evaluate(SymbolTable symbolTable) throws VariableInitializationException, BreakException, VariableDeclarationException;
}
