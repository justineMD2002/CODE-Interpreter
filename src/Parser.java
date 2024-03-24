import java.util.List;

class Parser {
    private List<Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentTokenIndex = 0;
    }

    // call parse method to start parsing
    public boolean parse() {
        return program();
    }

    // matches the type of the token
    private boolean match(Token.Type expectedType) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == expectedType) {

            System.out.println("Index: " + currentTokenIndex);
            currentTokenIndex++;
            return true;
        }
        // System.out.println(currentTokenIndex + " not matched");
        return false;
    }

    // Program --> BEGIN CODE VariableDeclarations ExecutableCode END CODE
    private boolean program() {
        if(match(Token.Type.BeginContainer)) {
            if(match(Token.Type.EndContainer)) {
                return true;
            } else {
                System.out.println(currentTokenIndex);
                if(variableDeclarations()) {
                    System.out.println("true");
                    return match(Token.Type.EndContainer);
                }
                return false;
            }
        }
        return false;
    }

    // VariableDeclarations -> VariableDeclaration VariableDeclarations | ε
    private boolean variableDeclarations() {
        if (variableDeclaration()) {
            variableDeclarations();
            return true;
        }
        return false;
    }

    // VariableDeclaration -> DataType VariableList
    private boolean variableDeclaration() {
        if (DataType()) {
            if (variableList()) {
                return true;
            }
            return false;
        }
        return false;
    }

    // VariableList -> VariableName VariableList'
    private boolean variableList() {
        if (variableName()) {
            if (variableListPrime()) {
                return true;
            }
            return false;
        }
        return false;
    }

    // VariableList' -> ',' VariableName VariableList' | ε
    private boolean variableListPrime() {
        if (match(Token.Type.Comma)) {
            if (variableName()) {
                return variableListPrime();
            }
            return false;
        }
        return true;
    }

    // DataType -> INT | CHAR | BOOL | FLOAT
    private boolean DataType() {
        return match(Token.Type.Int) || match(Token.Type.Char) || match(Token.Type.Bool) || match(Token.Type.Float);
    }

    // VariableName -> Identifier
    private boolean variableName() {
        if (match(Token.Type.Identifier)) {
            return assignment();
        }
        System.out.println("False");
        return false;
    }

    // Assignment -> '=' Expression
    private boolean assignment() {
        if (match(Token.Type.Assign)) {
            if (value()) {
                return true;
            }
            return false;
        }
        return true;
    }

    // Expression -> Identifier | Num
    private boolean value() {
        return match(Token.Type.Identifier) || match(Token.Type.Num) || match(Token.Type.NumFloat);
    }
}
