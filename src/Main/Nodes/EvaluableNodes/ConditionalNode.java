package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.ScannedInputException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.*;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class ConditionalNode extends EvaluableNode {
    private final List<ASTNode> conditions;
    private final ExecutableCodeNode ifStatements;
    private final List<ExecutableCodeNode> elseIfBlocks;
    private final ExecutableCodeNode elseStatements;
    private final int lineNumber;

    public ConditionalNode(List<ASTNode> conditions, ExecutableCodeNode ifStatements, List<ExecutableCodeNode> elseIfBlocks, ExecutableCodeNode elseStatements, int lineNumber) {
        this.conditions = conditions;
        this.ifStatements = ifStatements;
        this.elseIfBlocks = elseIfBlocks;
        this.elseStatements = elseStatements;
        this.lineNumber = lineNumber;
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

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableInitializationException, BreakException, VariableDeclarationException, ScannedInputException {
        List<ASTNode> executables = null;
        for(int i=0; i<getConditions().size(); i++) {
            ASTNode condition = getConditions().get(i);
            if(condition instanceof ExpressionNode expressionNode) {
                LiteralNode result = expressionNode.evaluateExpression(symbolTable);
                if (result.getValue().equals("TRUE")) {
                    executables = (i == 0) ? getIfStatements().getStatements() : getElseIfBlocks().get(i - 1).getStatements();
                    break;
                }
            } else if(condition instanceof VariableNode variableNode) {
                if(!symbolTable.getInitializedVariables().containsKey(variableNode.getVariableName())) {
                    throw new VariableDeclarationException("ERROR: Variable " + variableNode.getVariableName() + " is not declared.", getLineNumber());
                }
                LiteralNode result = symbolTable.getValue(variableNode.getVariableName());
                if(result.getValue() != null && result.getValue().equals("TRUE")) {
                    executables = (i == 0) ? getIfStatements().getStatements() : getElseIfBlocks().get(i-1).getStatements();
                    break;
                }
                throw new VariableInitializationException("ERROR: Variable " + variableNode.getVariableName() + " not initialized.", getLineNumber());
            } else if(condition instanceof LiteralNode literalNode) {
                if(literalNode.getValue().equals("TRUE")) {
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
            } else if(executable instanceof BreakNode) {
                throw new BreakException();
            }
        }

    }
}
