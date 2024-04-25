package Main.Nodes.ASTNodes;

import Main.ExceptionHandlers.DisplayException;
import Main.Nodes.SymbolTable;

public abstract class EvaluableNode extends ASTNode {
    public abstract void evaluate(SymbolTable symbolTable);
}
