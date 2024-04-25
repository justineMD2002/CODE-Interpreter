package Main.Nodes.ASTNodes;

import Main.Nodes.SymbolTable;

public class ProgramNode extends ASTNode {
    private final ASTNode variableDeclarations;
    private final ASTNode executableCode;

    private final SymbolTable symbolTable;

    public ProgramNode(ASTNode variableDeclarations, ASTNode executableCode, SymbolTable symbolTable) {
        this.variableDeclarations = variableDeclarations;
        this.executableCode = executableCode;
        this.symbolTable = symbolTable;
    }

    public ASTNode getVariableDeclarations() {
        return variableDeclarations;
    }

    public ASTNode getExecutableCode() {
        return executableCode;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

}
