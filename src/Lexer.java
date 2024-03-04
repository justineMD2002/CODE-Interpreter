import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
    private int currentPos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        while (currentPos < input.length()) {
            int tokenStartPos = currentPos;
            char lookahead = input.charAt(currentPos);
            if (Character.isWhitespace(lookahead)) {
                currentPos++;
            } else if(lookahead == '#') {
                while (currentPos < input.length() && input.charAt(currentPos)!= '\n') {
                    currentPos++;
                }
                currentPos++;
            } else if (lookahead == '+') {
                tokens.add(new Token(Token.Type.Plus, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '-') {
                tokens.add(new Token(Token.Type.Minus, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '>') {
                if(input.charAt(++currentPos) == '=')
                    tokens.add(new Token(Token.Type.GreaterEqual, Character.toString(lookahead) + input.charAt(currentPos), tokenStartPos));
                else 
                    tokens.add(new Token(Token.Type.Greater, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '<') {
                if(input.charAt(++currentPos) == '=')
                    tokens.add(new Token(Token.Type.LessEqual, Character.toString(lookahead) + input.charAt(currentPos), tokenStartPos));
                else if(input.charAt(currentPos) == '>')
                    tokens.add(new Token(Token.Type.NotEqual, Character.toString(lookahead) + input.charAt(currentPos), tokenStartPos));
                else 
                    tokens.add(new Token(Token.Type.Less, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '/') {
                tokens.add(new Token(Token.Type.Divide, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '(' || lookahead == ')'){
                tokens.add(new Token(Token.Type.Parentheses, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '[' || lookahead == ']'){
                tokens.add(new Token(Token.Type.SquareB, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '\''){
                tokens.add(new Token(Token.Type.SingleQ, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '\"'){
                tokens.add(new Token(Token.Type.DoubleQ, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '&'){
                tokens.add(new Token(Token.Type.Concat, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '$'){
                tokens.add(new Token(Token.Type.NewLine, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == ',') {
                tokens.add(new Token(Token.Type.Comma, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '%') {
                tokens.add(new Token(Token.Type.Modulo, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '*') {
                tokens.add(new Token(Token.Type.Times, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (lookahead == '=') {
                if(input.charAt(++currentPos) == '=')
                    tokens.add(new Token(Token.Type.Equals , Character.toString(lookahead) + input.charAt(currentPos), tokenStartPos));
                else 
                    tokens.add(new Token(Token.Type.Assign, Character.toString(lookahead), tokenStartPos));
                currentPos++;
            } else if (Character.isDigit(lookahead)) {
                StringBuilder text = new StringBuilder();
                while (currentPos < input.length() && Character.isDigit(input.charAt(currentPos))) {
                    text.append(input.charAt(currentPos));
                    currentPos++;
                }
                tokens.add(new Token(Token.Type.Num, text.toString(), tokenStartPos));
            } else if (Character.isLetter(lookahead)) {
                StringBuilder text = new StringBuilder();
                while (currentPos < input.length() && Character.isLetterOrDigit(input.charAt(currentPos))) {
                    text.append(input.charAt(currentPos));
                    currentPos++;
                    if(text.toString().equals("BEGIN") || text.toString().equals("END")) {
                        text.append(input.charAt(currentPos));
                        currentPos++;
                    }
                }
                Token.Type type;
                switch (text.toString()) {
                    case "AND":
                    type = Token.Type.And;
                    break;
                    case "DISPLAY":
                    type = Token.Type.Print;
                    break;
                    case "OR":
                    type = Token.Type.Or;
                    break;
                    case "NOT":
                    type = Token.Type.Not;
                    break;
                    case "INT":
                    type = Token.Type.Int;
                    break;
                    case "CHAR":
                    type = Token.Type.Char;
                    break;  
                    case "BOOL":
                    type = Token.Type.Bool;
                    break; 
                    case "FLOAT":
                    type = Token.Type.Float;
                    break;      
                    case "BEGIN CODE":
                    case "END CODE":
                        type = Token.Type.Container;
                        break;
                    default:
                        type = Token.Type.Identifier;
                }
                tokens.add(new Token(type, text.toString(), tokenStartPos));
            } else {
                throw new RuntimeException("Unknown character '" + lookahead + "' at position " + currentPos);
            }
        }
        tokens.add(new Token(Token.Type.EOF, "<EOF>", currentPos));
        printTokens(tokens);
        return tokens;
    }

    private void printTokens(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.println("Token " + i + ": Type = " + token.getType() + ", Text = '" + token.getText() + "', StartPos = " + token.getStartPos());
        }
    }

}
