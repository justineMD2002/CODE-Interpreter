package Main.Token.Lexer.Parser;

import Main.ExceptionHandlers.BreakException;
import Main.ExceptionHandlers.ScannedInputException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.*;
import Main.Nodes.AssignmentValidator;
import Main.Nodes.EvaluableNodes.VariableNode;
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

    public void analyze(ProgramNode programNode) throws VariableInitializationException, BreakException, VariableDeclarationException, ScannedInputException {
        analyze(programNode.getVariableDeclarations());
        analyze(programNode.getExecutableCode());
    }

    public void analyze(ASTNode node) throws VariableInitializationException, BreakException, VariableDeclarationException, ScannedInputException {
        if(node instanceof VariableDeclarationsNode variableDeclarationsNode) {
            List<SingleVariableDeclaration> declarations = variableDeclarationsNode.getVariableDeclarations();
            for(SingleVariableDeclaration declaration : declarations) {
                String dataType = declaration.getDataType();
                for(VariableNode variableNode : declaration.getVariableNames()) {
                    String variableName = variableNode.getVariableName();
                    switch (variableNode.getInitialValue()) {
                        case ExpressionNode expressionNode -> {
                            Object value = expressionNode.evaluateExpression(getSymbolTable()).getValue();
                            new VariableNode(variableName, value).evaluate(getSymbolTable());
                        }
                        case VariableNode varNode -> {
                            String varName = varNode.getVariableName();
                            LiteralNode initialValue;
                            if(!getSymbolTable().getInitializedVariables().containsKey(varName)) {
                                throw new VariableDeclarationException("ERROR: Variable " + varName + " is not declared.", declarations.indexOf(declaration)+1);
                            } else if(getSymbolTable().getValue(varName) == null || getSymbolTable().getValue(varName).getValue() == null) {
                                throw new VariableInitializationException("ERROR: Variable " + varName + " is not initialized.", declarations.indexOf(declaration)+1);
                            } else if(varNode.getInitialValue() == null) {
                                initialValue = new LiteralNode(getSymbolTable().getValue(varName).getValue());
                            } else if ((int) varNode.getInitialValue() == -1) {
                                if (getSymbolTable().getValue(varName).getValue() instanceof Integer) {
                                    initialValue = new LiteralNode((int) getSymbolTable().getValue(varName).getValue()*-1);
                                } else if (getSymbolTable().getValue(varName).getValue() instanceof Double) {
                                    initialValue = new LiteralNode((double) getSymbolTable().getValue(varName).getValue()*-1.0);
                                } else {
                                    throw new VariableInitializationException("ERROR: Value of type " + dataType + " cannot be negated.", declarations.indexOf(declaration)+1);
                                }
                            } else {
                                initialValue = new LiteralNode(varNode.getInitialValue());
                            }
                            AssignmentValidator.validateAssignmentType(dataType, variableNode.getVariableName(), initialValue, declarations.indexOf(declaration)+1);
                            new VariableNode(variableName, initialValue.getValue()).evaluate(getSymbolTable());
                        }
                        case LiteralNode literalNode -> {
                            AssignmentValidator.validateAssignmentType(dataType, variableNode.getVariableName(), literalNode, declarations.indexOf(declaration)+1);
                            new VariableNode(variableNode.getVariableName(), literalNode.getValue()).evaluate(getSymbolTable());
                        }
                        case null, default -> variableNode.evaluate(getSymbolTable());
                    }
                }
            }
        } else if(node instanceof ExecutableCodeNode executableCodeNode) {
            List<ASTNode> executables = executableCodeNode.getStatements();
            for(ASTNode statement : executables) {
                if(statement instanceof EvaluableNode evaluableNode) {
                    evaluableNode.evaluate(getSymbolTable());
                }
            }
        }
    }
}