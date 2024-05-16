package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ASTNodes.VariableDeclarationsNode;
import Main.Nodes.AssignmentValidator;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;
import java.util.NoSuchElementException;

public class VariableReinitializedNode extends EvaluableNode {
    private final List<String> variableNames;
    private LiteralNode value;
    private final VariableDeclarationsNode declaredVariables;
    private final int lineNumber;

    public VariableReinitializedNode(List<String> variableNames, LiteralNode value, VariableDeclarationsNode declaredVariables, int lineNumber) {
        this.variableNames = variableNames;
        this.value = value;
        this.declaredVariables = declaredVariables;
        this.lineNumber = lineNumber;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public LiteralNode getValue() {
        return value;
    }

    public void setValue(LiteralNode value) {
        this.value = value;
    }

    public VariableDeclarationsNode getDeclaredVariables() {
        return declaredVariables;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws VariableDeclarationException, VariableInitializationException {
        LiteralNode currValue = getValue();
        for(String varName : getVariableNames()) {
            LiteralNode value = getValue();
            if(value.getValue() instanceof ExpressionNode expressionNode) {
                value = expressionNode.evaluateExpression(symbolTable);
            } else if(value.getValue() instanceof VariableNode variableNode) {
                String variableNodeName = variableNode.getVariableName();
                if (!symbolTable.getInitializedVariables().containsKey(variableNodeName)) {
                    throw new VariableDeclarationException("ERROR: Variable '" + variableNodeName + "' not declared.", getLineNumber());
                } else if(symbolTable.getValue(variableNodeName) != null && symbolTable.getValue(variableNodeName).getValue() != null) {
                     if(variableNode.getInitialValue() == null) {
                        value = symbolTable.getValue(variableNodeName);
                    } else if((int)variableNode.getInitialValue() == -1) {
                        if(variableNode.getInitialValue() instanceof Integer) {
                            value = new LiteralNode((int) symbolTable.getValue(variableNodeName).getValue()*-1);
                        } else if(variableNode.getInitialValue() instanceof Double) {
                            value = new LiteralNode((double) symbolTable.getValue(variableNodeName).getValue()*-1.0);
                        }
                    }
                } else {
                    throw new VariableDeclarationException("ERROR: Variable '" + variableNodeName + "' not initialized.", getLineNumber());
                }

            }

            setValue(value);
            String dataType = getDeclaredVariables().getVariableDeclarations().stream()
                    .filter(declaration -> declaration.getVariableNames().stream().anyMatch(variable -> variable.getVariableName().equals(varName)))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No value present"))
                    .getDataType();

            AssignmentValidator.validateAssignmentType(dataType, varName, new LiteralNode(value.getValue()), getLineNumber());
            symbolTable.setValue(varName, value);
        }
        setValue(currValue);
    }

}
