package Main.Token.Lexer.Parser;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.*;
import Main.Token.Token;

import java.util.ArrayList;
import java.util.List;

public  class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentTokenIndex = 0;
    }

    // call parse method to start parsing
    public ASTNode parse() throws VariableDeclarationException, VariableInitializationException {
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
    private ASTNode program() throws VariableDeclarationException, VariableInitializationException {
        if(match(Token.Type.BeginContainer)) {
            ASTNode variableDeclarations = variableDeclarations();
            ASTNode executableCode = executableCode();
            if(match(Token.Type.EndContainer)) {
                return new ProgramNode(variableDeclarations, executableCode);
            }
        }
        return null;
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
            List<String> variables = variableList();
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
            if(value() != null) {
                return value();
            }
            throw new VariableInitializationException("Assignment operator found but " +
                    "value token is missing. Please check again");
        }
        return null;
    }

    // Expression -> Num | NumFloat | CharLiteral | BooleanLiteral
    private Object value() {
        if (match(Token.Type.Num) || match(Token.Type.NumFloat) || match(Token.Type.CharLiteral) || match(Token.Type.BooleanLiteral)) {
            return tokens.get(currentTokenIndex - 1).getText();
        }
        return null;
    }

    // variable initializer
    private void initializeVariable(String varName, LiteralNode value) {
        VariableInitializerNode variableInitializer = new VariableInitializerNode();
        variableInitializer.setValue(varName, value);
    }

    private ASTNode executableCode() {
        return null;
    }
}
