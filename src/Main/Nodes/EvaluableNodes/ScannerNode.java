package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.ScannedInputException;
import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ASTNodes.SingleVariableDeclaration;
import Main.Nodes.ASTNodes.VariableDeclarationsNode;
import Main.Nodes.AssignmentValidator;
import Main.Nodes.SymbolTable;

import java.util.List;
import java.util.Scanner;

public class ScannerNode extends EvaluableNode {
    private final List<String> variableNames;
    private final VariableDeclarationsNode declarations;
    private final int lineNumber;

    public ScannerNode(List<String> variableNames, VariableDeclarationsNode declarations, int lineNumber) {
        this.variableNames = variableNames;
        this.declarations = declarations;
        this.lineNumber = lineNumber;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public VariableDeclarationsNode getDeclarations() {
        return declarations;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) throws ScannedInputException, VariableInitializationException, VariableDeclarationException {
        String[] userInputValues = getStrings(getVariableNames());

        for (int i = 0; i < getVariableNames().size(); i++) {
            String variableName = getVariableNames().get(i);
            String userInput = userInputValues[i].trim(); // Remove any leading/trailing whitespace
            Object parsedValue = parseInput(userInput);
            if (parsedValue != null) {
                LiteralNode valueNode = new LiteralNode(parsedValue);
                symbolTable.setValue(variableName, valueNode);
                String dataType = getDataType(variableName, getDeclarations().getVariableDeclarations());
                AssignmentValidator.validateAssignmentType(dataType, variableName, valueNode, getLineNumber());
            } else {
                throw new ScannedInputException("ERROR: Invalid input provided for variable '" + variableName + "'.", getLineNumber());
            }
        }


    }

    private String[] getStrings(List<String> variableNames) throws ScannedInputException {
        Scanner scanner = new Scanner(System.in);

        // Prompt for input once, for all variables
        String inputLine = scanner.nextLine();
        String[] userInputValues = inputLine.split(",");

        if (userInputValues.length != variableNames.size()) {
            throw new ScannedInputException("ERROR: The number of values provided does not match the number of variables.", getLineNumber());
        }

        return userInputValues;
    }

    private Object parseInput(String userInput) {
        // Try to parse user input to various data types
        if (userInput.matches("^-?\\d+$")) {
            return Integer.parseInt(userInput); // Integer
        } else if (userInput.matches("^-?\\d*\\.\\d+$")) {
            return Float.parseFloat(userInput); // Float
        } else if (userInput.equalsIgnoreCase("true") || userInput.equalsIgnoreCase("false")) {
            return userInput; // Boolean
        } else if (userInput.length() == 1) {
            return userInput.charAt(0); // Character
        }
        return null; // Invalid input
    }


    private String getDataType(String variableName, List<SingleVariableDeclaration> declarations) throws VariableDeclarationException {
        for (SingleVariableDeclaration declaration : declarations) {
            if (declaration.getVariableNames().stream().anyMatch(variableNode -> variableNode.getVariableName().equals(variableName))) {
                return declaration.getDataType();
            }
        }
        throw new VariableDeclarationException("ERROR: Variable '" + variableName + "' not declared.", getLineNumber());
    }



}
