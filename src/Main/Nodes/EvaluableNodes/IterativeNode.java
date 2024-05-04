package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.ASTNode;
import Main.Nodes.ASTNodes.BreakNode;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class IterativeNode extends EvaluableNode {
    private final ASTNode condition;
    private final List<ASTNode> whileStatements;

    public IterativeNode(ASTNode condition, List<ASTNode> whileStatements) {
        this.condition = condition;
        this.whileStatements = whileStatements;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public List<ASTNode> getWhileStatements() {
        return whileStatements;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableInitializationException, VariableDeclarationException {
        ASTNode condition = getCondition();
        boolean conditionResult = updateCondition(condition, symbolTable);


        while (conditionResult) {
            try {
                for (ASTNode statement : getWhileStatements()) {
                    if(statement instanceof EvaluableNode evaluableNode) {
                        evaluableNode.evaluate(symbolTable);
                    } else if(statement instanceof BreakNode) {
                        throw new BreakException();
                    }
                }
                conditionResult = updateCondition(condition, symbolTable);
            } catch (BreakException e) {
                break;
            }
        }
    }

    private boolean updateCondition(ASTNode condition, SymbolTable symbolTable) throws VariableInitializationException, VariableDeclarationException {
        if(condition instanceof ExpressionNode expressionNode) {
            LiteralNode result = expressionNode.evaluateExpression(symbolTable);
            return result.getValue().equals("TRUE");
        } else if(condition instanceof VariableNode variableNode) {
            LiteralNode result = symbolTable.getValue(variableNode.getVariableName());
            if(result.getValue() != null) {
                return result.getValue().equals("TRUE");
            }
            throw new VariableInitializationException("ERROR: Variable " + variableNode.getVariableName() + " not initialized.");
        } else if(condition instanceof LiteralNode literalNode) {
            return literalNode.getValue().equals("TRUE");
        }
        return false;
    }
}
