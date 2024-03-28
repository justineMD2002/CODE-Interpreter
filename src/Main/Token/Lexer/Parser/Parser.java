package Main.Token.Lexer.Parser;

import Main.ExceptionHandlers.VariableDeclarationException;
import Main.Nodes.*;
import Main.Token.Token;

import java.util.ArrayList;
import java.util.List;

public  class Parser {
    private List<Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentTokenIndex = 0;
    }

    // call parse method to start parsing
    public ASTNode parse() {
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
    private ASTNode program() {
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
    private ASTNode variableDeclarations() {
        List<SingleVariableDeclaration> variables = new ArrayList<>();

        while (true) {
            try {
                SingleVariableDeclaration variable = (SingleVariableDeclaration) variableDeclaration();
                if (variable != null) {
                    variables.add(variable);
                } else {
                    break;
                }
            } catch (VariableDeclarationException v) {
                System.out.println(v.getMessage());
                v.printStackTrace();
            }
        }

        return new VariableDeclarationsNode(variables);
    }

    // VariableDeclaration -> DataType VariableList
    private ASTNode variableDeclaration() throws VariableDeclarationException {
        String dataType = tokens.get(currentTokenIndex).getText();
        ASTNode variableDeclaration = null;
        if (!dataType.isEmpty() && DataType()) {
            List<String> variables = variableList();
            if(variables.isEmpty()) {
                throw new VariableDeclarationException();
            }
            variableDeclaration = new SingleVariableDeclaration(dataType, variables);
        }
        return variableDeclaration;
    }

    // VariableList -> VariableName VariableList'
    private List<String> variableList() {
        List<String> variableNames = new ArrayList<>();
        String variableName = variableName();
        if (!variableName.isEmpty()) {
            variableNames.add(variableName);
            if (match(Token.Type.Comma)) {
                variableNames.addAll(variableList());
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
        String variableName = tokens.get(currentTokenIndex).getText();
        if (match(Token.Type.Identifier)) {

        }
        return false;
    }

    // Assignment -> '=' Expression
    private ASTNode assignment() {
        if (match(Token.Type.Assign)) {
            return value();
        }
        return true;
    }

    // Expression -> Num | NumFloat | CharLiteral | BooleanLiteral
    private boolean value() {
        return match(Token.Type.Num) || match(Token.Type.NumFloat) || match(Token.Type.CharLiteral) || match(Token.Type.BooleanLiteral);
    }

    private ASTNode executableCode() {
        return null;
    }
}
