package Main.Nodes.EvaluableNodes;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.EvaluableNode;
import Main.Nodes.ASTNodes.LiteralNode;
import Main.Nodes.ASTNodes.SingleVariableDeclaration;
import Main.Nodes.ASTNodes.VariableDeclarationsNode;
import Main.Nodes.AssignmentValidator;
import Main.Nodes.SymbolTable;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ScannerNode extends EvaluableNode {
    private final List<String> variableNames;
    private final VariableDeclarationsNode declarations;

    public ScannerNode(List<String> variableNames, VariableDeclarationsNode declarations) {
        this.variableNames = variableNames;
        this.declarations = declarations;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    @Override
    public void evaluate(SymbolTable symbolTable) {
        try {
            String[] userInputValues = getStrings(variableNames);

            for (int i = 0; i < variableNames.size(); i++) {
                String variableName = variableNames.get(i);
                String userInput = userInputValues[i].trim(); // Remove any leading/trailing whitespace
                Object parsedValue = parseInput(userInput);
                if (parsedValue != null) {
                    LiteralNode valueNode = new LiteralNode(parsedValue);
                    symbolTable.setValue(variableName, valueNode);
                    String dataType = getDataType(variableName, declarations.getVariableDeclarations());
                    AssignmentValidator.validateAssignmentType(dataType, variableName, valueNode);
                } else {
                    throw new InputMismatchException("ERROR: No input provided for variable '" + variableName + "'.");
                }
            }
        } catch ( InputMismatchException | VariableInitializationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    private String[] getStrings(List<String> variableNames) {
        Scanner scanner = new Scanner(System.in);

        // Prompt for input once, for all variables
        String inputLine = scanner.nextLine();
        String[] userInputValues = inputLine.split(",");

        try {
            if (userInputValues.length != variableNames.size()) {
                throw new InputMismatchException("ERROR: The number of values provided does not match the number of variables.");
            }
        } catch (InputMismatchException i) {
            System.err.println(i.getMessage());
            System.exit(1);
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


    private String getDataType(String variableName, List<SingleVariableDeclaration> declarations) {
        try {
            for (SingleVariableDeclaration declaration : declarations) {
                if (declaration.getVariableNames().stream().anyMatch(variableNode -> variableNode.getVariableName().equals(variableName))) {
                    return declaration.getDataType();
                }
            }
            throw new VariableDeclarationException("ERROR: Variable '" + variableName + "' not declared.");
        } catch (VariableDeclarationException v) {
            System.err.println(v.getMessage());
            System.exit(1);
        }
        return null;
    }



}
