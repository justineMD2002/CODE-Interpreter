package Main.Token.Lexer.Parser;

import Main.ExceptionHandlers.*;
import Main.Nodes.*;
import Main.Token.Token;

import java.util.*;

public  class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex;
    private final VariableInitializerNode variableInitializer = new VariableInitializerNode();



    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentTokenIndex = 0;
    }



    // call parse method to start parsing
    public ASTNode parse() throws VariableDeclarationException, VariableInitializationException, DisplayException, BeginContainerMissingException, EndContainerMissingException {
        return program();
    }




    // matches the type of the token
    private boolean match(Token.Type expectedType) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == expectedType) {
            currentTokenIndex++;
            return true;
        }
        // System.out.println(currentTokenIndex + " not matched");
        return false;
    }





    // Program --> BEGIN CODE VariableDeclarations ExecutableCode END CODE
    private ASTNode program() throws VariableDeclarationException, VariableInitializationException, DisplayException, BeginContainerMissingException, EndContainerMissingException {
        if(match(Token.Type.BeginContainer)) {
            ASTNode variableDeclarations = variableDeclarations();
            ASTNode executableCode = executableCode();
            if(match(Token.Type.EndContainer)) {
                return new ProgramNode(variableDeclarations, executableCode);
            } else {
                throw new EndContainerMissingException("Code should end with an \"END CODE\" syntax.");
            }
        } else {
            throw new BeginContainerMissingException("Code should begin with a \"BEGIN CODE\" syntax.");
        }
    }



    // VariableDeclarations -> VariableDeclaration VariableDeclarations | ε
    private ASTNode variableDeclarations() throws VariableDeclarationException, VariableInitializationException {
        List<SingleVariableDeclaration> variables = new ArrayList<>();

        while (true) {
            try {
                SingleVariableDeclaration variable = (SingleVariableDeclaration) variableDeclaration();
                if (variable != null) {
                    variables.add(variable);
                } else {
                    break;
                }
            } catch (VariableDeclarationException | VariableInitializationException v) {
                v.printStackTrace();
                throw v;
            }
        }

        return new VariableDeclarationsNode(variables);
    }




    // VariableDeclaration -> DataType VariableList
    private ASTNode variableDeclaration() throws VariableDeclarationException, VariableInitializationException {
        String dataType = tokens.get(currentTokenIndex).getText();
        ASTNode variableDeclaration = null;
        if (!dataType.isEmpty() && DataType()) {
//            System.out.println("dtype:" + dataType);
            List<String> variables = variableList();
//            System.out.println("variables:" + variables);
            for(String variable : variables) {
                validateAssignmentType(dataType, variable);
            }
            if(variables.isEmpty()) {
                throw new VariableDeclarationException("Error: Found Data Type token but variable list is empty.");
            }
            variableDeclaration = new SingleVariableDeclaration(dataType, variables);
        }
        return variableDeclaration;
    }




    // VariableList -> VariableName VariableList'
    private List<String> variableList() throws VariableDeclarationException, VariableInitializationException {
        List<String> variableNames = new ArrayList<>();
        String variableName = variableName();
        if (variableName != null) {
            /*
                Check first for the first variable
                Also include whether it is initialized
                on declaration
             */
            Object assignmentValue = assignment();
            LiteralNode value;
            if(assignmentValue != null) {
//                System.out.println("not null");
                value = new LiteralNode(assignmentValue);
                initializeVariable(variableName, value);
            }
            /*
             *  */
            variableNames.add(variableName);
            /*     */

            while (match(Token.Type.Comma)) {
                variableName = variableName();
                if (variableName != null) {
                    assignmentValue = assignment();
                    if(assignmentValue != null) {
//                        System.out.println(variableName + "=" + assignmentValue);
                        value = new LiteralNode(assignmentValue);
                        initializeVariable(variableName, value);
                    }
                    variableNames.add(variableName);
                } else {
                    throw new VariableDeclarationException("Error parsing variable declarations. " +
                            "Found a comma token without a succeeding variable declaration");
                }

            }
        }
        return variableNames;
    }



    // DataType -> INT | CHAR | BOOL | FLOAT
    private boolean DataType() {
        return match(Token.Type.Int) || match(Token.Type.Char) || match(Token.Type.Bool) || match(Token.Type.Float);
    }




    // VariableName -> Identifier
    private String variableName() {
        String variableName = null;
        if (match(Token.Type.Identifier)) {
            variableName = tokens.get(currentTokenIndex - 1).getText(); // Get the variable name
        }
        return variableName;
    }




    // Assignment -> '=' Expression
    private Object assignment() throws VariableInitializationException {
        if (match(Token.Type.Assign)) {
//            System.out.println("assignment");
            Object assignedValue = value();
            if(assignedValue != null) {
                return assignedValue;
            }
            throw new VariableInitializationException("Assignment operator found but " +
                    "value token is missing. Please check again");
        }
        return null;
    }




    // Expression -> Num | NumFloat | CharLiteral | BooleanLiteral
    private Object value() {
        String value = tokens.get(currentTokenIndex).getText();
        if (match(Token.Type.Num) || match(Token.Type.NumFloat) || match(Token.Type.CharLiteral) || match(Token.Type.BooleanLiteral)) {
            return value;
        }
        return null;
    }




    private void validateAssignmentType(String dataType, String variable) throws VariableInitializationException {
        LiteralNode valueNode = variableInitializer.getValue(variable);
        if (valueNode != null) {
            Object assignedValue = valueNode.getValue();
            if (assignedValue != null) {
                // Check if the assigned value matches the data type
                if ("INT".equals(dataType)) {
                    try {
                        int intValue = Integer.parseInt(String.valueOf(assignedValue));
                        // The assigned value can be parsed as an int, so it matches the data type INT
                    } catch (NumberFormatException e) {
                        throw new VariableInitializationException("Error: Assigned value for variable '" + variable + "' is not a valid integer.");
                    }
                } else if ("CHAR".equals(dataType)) {
                    if (!(assignedValue instanceof Character)) {
                        throw new VariableInitializationException("Error: Assigned value for variable '" + variable + "' does not match data type CHAR.");
                    }
                } else if ("BOOL".equals(dataType)) {
                    if (!(assignedValue.equals("FALSE")||assignedValue.equals("TRUE"))) {
                        throw new VariableInitializationException("Error: Assigned value for variable '" + variable + "' does not match data type BOOL.");
                    }
                } else if ("FLOAT".equals(dataType)) {
                    try {
                        double floatValue = Double.parseDouble(String.valueOf(assignedValue));
                        // The assigned value can be parsed as a double, so it matches the data type FLOAT
                    } catch (NumberFormatException e) {
                        throw new VariableInitializationException("Error: Assigned value for variable '" + variable + "' is not a valid float.");
                    }
                } else {
                    throw new VariableInitializationException("Error: Unsupported data type '" + dataType + "'.");
                }
            }
        } else {
            // Variable is declared but not initialized
            System.out.println("Warning: Variable '" + variable + "' is declared but not initialized.");
        }
    }






    // variable initializer
    private void initializeVariable(String varName, LiteralNode value) {
        variableInitializer.setValue(varName, value);
    }

    private ASTNode executableCode() throws DisplayException {
        return displayFunction();
    }

    private DisplayNode displayFunction() throws DisplayException {
        if(match(Token.Type.Print)) {
            StringBuilder stringBuilder = new StringBuilder();
            if(match(Token.Type.Colon)) {
                while(match(Token.Type.Identifier)) {
                    String variableName = tokens.get(currentTokenIndex - 1).getText();
                    LiteralNode value = variableInitializer.getValue(variableName);

                    if (value != null) {
                        stringBuilder.append(value.getValue()); // Append the value to the StringBuilder
                    } else {
                        throw new DisplayException("Variable '" + variableName + "' is not initialized.");
                    }

                    if (!match(Token.Type.Concat)) {
                        break;
                    }
                }
                return new DisplayNode(stringBuilder.toString());
            }

        }
        return null;
    }
}