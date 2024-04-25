package Main.Token.Lexer.Parser;

import Main.ExceptionHandlers.DisplayException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.*;
import Main.Nodes.AssignmentValidator;
import Main.Nodes.EvaluableNodes.DisplayNode;
import Main.Nodes.EvaluableNodes.ScannerNode;
import Main.Nodes.EvaluableNodes.VariableNode;
import Main.Nodes.EvaluableNodes.VariableReinitializedNode;
import Main.Nodes.ExpressionNodes.ExpressionNode;
import Main.Nodes.SymbolTable;

import java.util.List;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void analyze(ProgramNode programNode) throws VariableDeclarationException, VariableInitializationException {
        analyze(programNode.getVariableDeclarations());
        analyze(programNode.getExecutableCode());
    }

    public void analyze(ASTNode node) throws VariableDeclarationException, VariableInitializationException {
        if(node instanceof VariableDeclarationsNode variableDeclarationsNode) {
            List<SingleVariableDeclaration> declarations = variableDeclarationsNode.getVariableDeclarations();
            for(SingleVariableDeclaration declaration : declarations) {
                String dataType = declaration.getDataType();
                for(VariableNode variableNode : declaration.getVariableNames()) {
                    String variableName = variableNode.getVariableName();
                    if(symbolTable.getInitializedVariables().containsKey(variableName)) {
                        throw new VariableDeclarationException("ERROR: Variable '" + variableName + "' has already been declared.");
                    } else if(variableNode.getInitialValue() instanceof ExpressionNode expressionNode) {
                        Object value = expressionNode.evaluateExpression(getSymbolTable()).getValue();
                        new VariableNode(variableName, value).evaluate(getSymbolTable()); ;
                    } else if(variableNode.getInitialValue() instanceof VariableNode varNode) {
                        String varName = varNode.getVariableName();
                        Object initialValue;
                        if((int)varNode.getInitialValue() == -1) {
                            if(varNode.getInitialValue() instanceof Integer) {
                                initialValue = (int) varNode.getInitialValue()*-1;
                            } else if(varNode.getInitialValue() instanceof Double) {
                                initialValue = (double) varNode.getInitialValue()*-1.0;
                            } else {
                                throw new VariableInitializationException("ERROR: Value of type " + dataType + " cannot be negated.");
                            }
                        } else {
                            initialValue = getSymbolTable().getValue(varName);
                        }
                        AssignmentValidator.validateAssignmentType(dataType, variableNode.getVariableName(), new LiteralNode(initialValue));
                        new VariableNode(variableName, initialValue).evaluate(getSymbolTable());
                    } else if(variableNode.getInitialValue() instanceof LiteralNode literalNode) {
                        AssignmentValidator.validateAssignmentType(dataType, variableNode.getVariableName(), literalNode);
                        new VariableNode(variableNode.getVariableName(), literalNode.getValue()).evaluate(symbolTable);
                    } else {
                        variableNode.evaluate(getSymbolTable());
                    }
                }
            }
        } else if(node instanceof ExecutableCodeNode executableCodeNode) {
            List<ASTNode> executables = executableCodeNode.getStatements();
            for(ASTNode statement : executables) {
                if(statement instanceof EvaluableNode evaluableNode) {
                    evaluableNode.evaluate(symbolTable);
                }
            }
        }
    }
}