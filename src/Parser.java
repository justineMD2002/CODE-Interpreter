import java.util.List;

class Parser {
    private List<Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentTokenIndex = 0;
    }

    // call parse method to start parsing
    public void parse() {
        program();
    }

    //matches the type of the token
    private boolean match(Token.Type expectedType) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == expectedType) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }

    // Program --> BEGIN CODE VariableDeclarations ExecutableCode END CODE
    public boolean program() {
        return match(Token.Type.BeginContainer) && (match(Token.Type.Int) || match(Token.Type.Char)
                || match(Token.Type.Float) || match(Token.Type.Bool)) && match(Token.Type.EndContainer);
    }
}
