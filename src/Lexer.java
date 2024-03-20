import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            int temp = currentPos;
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
                if(input.charAt(++temp) == '=') {
                    tokens.add(new Token(Token.Type.GreaterEqual, Character.toString(lookahead) + input.charAt(temp), tokenStartPos));
                    currentPos++;
                } else {
                    tokens.add(new Token(Token.Type.Greater, Character.toString(lookahead), tokenStartPos));
                } currentPos++;
            } else if (lookahead == '<') {
                if(input.charAt(++temp) == '=') {
                    tokens.add(new Token(Token.Type.LessEqual, Character.toString(lookahead) + input.charAt(temp), tokenStartPos));
                    currentPos++;
                } else if(input.charAt(temp) == '>') {
                    tokens.add(new Token(Token.Type.NotEqual, Character.toString(lookahead) + input.charAt(temp), tokenStartPos));
                    currentPos++;
                } else {
                    tokens.add(new Token(Token.Type.Less, Character.toString(lookahead), tokenStartPos));
                } currentPos++;
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
                if(input.charAt(++temp) == '=') {
                    tokens.add(new Token(Token.Type.Equals , Character.toString(lookahead) + input.charAt(temp), tokenStartPos));
                    currentPos++;
                } else {
                    tokens.add(new Token(Token.Type.Assign, Character.toString(lookahead), tokenStartPos));
                } currentPos++;
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
                    else if(text.toString().equals("ELSE") && (currentPos + 2 < input.length()) && input.substring((currentPos+1), (currentPos+3)).equals("IF")) {
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
                    case "SCAN":
                        type = Token.Type.Scan;
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
                    case "TRUE":
                    case "FALSE":
                        type = Token.Type.BooleanLiteral;
                        break;       
                    case "BEGIN CODE":
                        type = Token.Type.BeginContainer;
                        break;
                    case "END CODE":
                        type = Token.Type.EndContainer;
                        break;
                    case "BEGIN IF":
                        type = Token.Type.BeginIf;
                        break;
                    case "END IF":
                        type = Token.Type.EndIf;
                        break;
                    case "BEGIN WHILE":
                        type = Token.Type.BeginWhile;
                        break;
                    case "END WHILE":
                        type = Token.Type.EndWhile;
                        break;
                    case "ELSE IF":
                        type = Token.Type.IfElse;
                        break;
                    case "IF":
                        type = Token.Type.If;
                        break;
                    case "ELSE":
                        type = Token.Type.Else;
                        break;
                    default:
                        type = Token.Type.Identifier;
                }
                tokens.add(new Token(type, text.toString(), tokenStartPos));
            } else {
                throw new RuntimeException("Unknown character '" + lookahead + "' at position " + currentPos);
            }
        }
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
