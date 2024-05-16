package Main.Nodes.EvaluableNodes.IterativeNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.ScannedInputException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.*;
import Main.Nodes.EvaluableNodes.VariableNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public abstract class IterativeNode extends EvaluableNode {
    private final ASTNode condition;
    private final List<ASTNode> iterativeStatements;
    private final int lineNumber;

    public IterativeNode(ASTNode condition, List<ASTNode> iterativeStatements, int lineNumber) {
        this.condition = condition;
        this.iterativeStatements = iterativeStatements;
        this.lineNumber = lineNumber;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public List<ASTNode> getIterativeStatements() {
        return iterativeStatements;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    protected void evaluateLoop(SymbolTable symbolTable, ASTNode condition) throws VariableInitializationException, VariableDeclarationException, ScannedInputException {
        boolean conditionResult = updateCondition(condition, symbolTable);

        while (conditionResult) {
            try {
                for (ASTNode statement : getIterativeStatements()) {
                    if(statement instanceof EvaluableNode evaluableNode) {
                        evaluableNode.evaluate(symbolTable);
                    } else if(statement instanceof BreakNode) {
                        throw new BreakException();
                    } else if(statement instanceof ContinueNode) {
                        break;
                    }
                }
                conditionResult = updateCondition(condition, symbolTable);
            } catch (BreakException e) {
                break;
            }
        }
    }

    protected boolean updateCondition(ASTNode condition, SymbolTable symbolTable) throws VariableInitializationException, VariableDeclarationException {
        if(condition instanceof ExpressionNode expressionNode) {
            LiteralNode result = expressionNode.evaluateExpression(symbolTable);
            return result.getValue().equals("TRUE");
        } else if(condition instanceof VariableNode variableNode) {
            if(!symbolTable.getInitializedVariables().containsKey(variableNode.getVariableName())) {
                throw new VariableDeclarationException("ERROR: Variable " + variableNode.getVariableName() + " is not declared.", getLineNumber());
            }
            LiteralNode result = symbolTable.getValue(variableNode.getVariableName());
            if(result.getValue() != null) {
                return result.getValue().equals("TRUE");
            }
            throw new VariableInitializationException("ERROR: Variable " + variableNode.getVariableName() + " not initialized.", getLineNumber());
        } else if(condition instanceof LiteralNode literalNode) {
            return literalNode.getValue().equals("TRUE");
        }
        return false;
    }
}
