package Main.Token.Lexer;

import Main.Token.Token;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int currentPos = 0;
    private int lineCount;

    public Lexer(String input) {
        this.input = input;
        lineCount = 0;
    }

    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        lineCount = input.split("\n").length;
        while (currentPos < input.length()) {
            int tokenStartPos = currentPos;
            char lookahead = input.charAt(currentPos);
            if (Character.isWhitespace(lookahead)) {
                currentPos++;
                continue;
            }
            switch (lookahead) {
                case '#':
                    skipComment();
                    break;
                case ':':
                case '+':
                case '-':
                case '(':
                case ')':
                case '[':
                case ']':
                case '\'':
                case '\"':
                case '&':
                case ',':
                case '%':
                case '*':
                    addSingleCharacterToken(tokens, lookahead, tokenStartPos);
                    currentPos++;
                    break;
                case '>':
                    handleGreaterToken(tokens, tokenStartPos);
                    break;
                case '<':
                    handleLessToken(tokens, tokenStartPos);
                    break;
                case '/':
                    tokens.add(new Token(Token.Type.Divide, Character.toString(lookahead), tokenStartPos));
                    currentPos++;
                    break;
                case '$':
                    tokens.add(new Token(Token.Type.NewLine, Character.toString(lookahead), tokenStartPos));
                    currentPos++;
                    break;
                case '=':
                    handleEqualsToken(tokens, tokenStartPos);
                    break;
                default:
                    if (Character.isDigit(lookahead)) {
                        handleNumberToken(tokens, tokenStartPos);
                    } else if (Character.isLetter(lookahead) || lookahead == '_') {
                        handleIdentifierOrKeyword(tokens, tokenStartPos);
                    } else {
                        throw new RuntimeException("ERROR: Unknown character '" + lookahead + "' at position " + currentPos);
                    }
            }
        }
//            printTokens(tokens);
        return tokens;
    }
    

    public int getLineCount() {
        return lineCount;
    }

    private void skipComment() {
        while (currentPos < input.length() && input.charAt(currentPos) != '\n') {
            currentPos++;
        }
    }

    private boolean isValidEscapeChar(char c) {
        return c == '$' || c == '&' || c == '[' || c == ']' || c == '\'' || c == '"' || c == '#';
    }

    private void addSingleCharacterToken(List<Token> tokens, char character, int startPos) {
        Token.Type type;

        if(character == '_') handleIdentifierOrKeyword(tokens, startPos);

        if (character == '[') {
            StringBuilder charLiteralBuilder = new StringBuilder();
            char nextChar = input.charAt(currentPos + 1);

            if (currentPos + 1 < input.length() && isValidEscapeChar(nextChar)) {
                charLiteralBuilder.append(nextChar);
                currentPos++;

                if (currentPos + 1 < input.length() && input.charAt(currentPos + 1) == ']') {
                    String charLiteral = charLiteralBuilder.toString();
                    tokens.add(new Token(Token.Type.Escape, charLiteral, startPos));
                    currentPos += 2;
                    return;
                } else {
                    throw new RuntimeException("ERROR: Unclosed or invalid character literal starting at position " + startPos);
                }
            } else {
                throw new RuntimeException("ERROR: Invalid character following '[' at position " + (startPos + 1));
            }
        }

        if(character == '\'') {
            StringBuilder charLiteralBuilder = new StringBuilder();

            while (currentPos + 1 < input.length() && input.charAt(currentPos + 1) != '\'') {
                char nextChar = input.charAt(currentPos + 1);
                charLiteralBuilder.append(nextChar);
                currentPos++;
            }

            if (currentPos + 1 < input.length() && input.charAt(currentPos + 1) == '\'') {
                String charLiteral = charLiteralBuilder.toString();
                tokens.add(new Token(Token.Type.CharLiteral, charLiteral, startPos));
                currentPos += 1;
                return;
            } else {
                throw new RuntimeException("ERROR: Unclosed character literal starting at position " + startPos);
            }
        }

        if(character == '\"') {
            StringBuilder charLiteralBuilder = new StringBuilder();

            while (currentPos + 1 < input.length() && input.charAt(currentPos + 1) != '\"') {
                char nextChar = input.charAt(currentPos + 1);
                charLiteralBuilder.append(nextChar);
                currentPos++;
            }

            if (currentPos + 1 < input.length() && input.charAt(currentPos + 1) == '\"') {
                String charLiteral = charLiteralBuilder.toString();
                if(charLiteral.equals("TRUE") || charLiteral.equals("FALSE")) {
                    tokens.add(new Token(Token.Type.BooleanLiteral, charLiteral, startPos));
                } else {
                    tokens.add(new Token(Token.Type.StringLiteral, charLiteral, startPos));
                }
                currentPos += 1;
                return;
            } else {
                throw new RuntimeException("ERROR: Unclosed character literal starting at position " + startPos);
            }
        }

        switch (character) {
            case ':':
                type = Token.Type.Colon;
                break;
            case '+':
                type = Token.Type.Plus;
                break;
            case '-':
                if(currentPos < input.length() && (tokens.getLast().getType() == Token.Type.Num ||
                        tokens.getLast().getType() == Token.Type.NumFloat ||
                        tokens.getLast().getType() == Token.Type.Identifier) ||
                        (tokens.getLast().getType() == Token.Type.Parentheses && tokens.getLast().getText().equals(")")) ) {
                    type = Token.Type.Minus;
                }
                else {
                    type = Token.Type.Negation;
                }
                break;
            case '(':
            case ')':
                type = Token.Type.Parentheses;
                break;
            case '&':
                type = Token.Type.Concat;
                break;
            case ',':
                type = Token.Type.Comma;
                break;
            case '%':
                type = Token.Type.Modulo;
                break;
            case '*':
                type = Token.Type.Times;
                break;
            default:
                throw new RuntimeException("ERROR: Unknown character '" + character + "' at position " + currentPos);
        }
        tokens.add(new Token(type, Character.toString(character), startPos));
    }

    private void handleGreaterToken(List<Token> tokens, int tokenStartPos) {
        if (currentPos + 1 < input.length()) {
            int temp = currentPos;
            if (input.charAt(++temp) == '=') {
                tokens.add(new Token(Token.Type.GreaterEqual, ">=", tokenStartPos));
                currentPos++;
            } else {
                tokens.add(new Token(Token.Type.Greater, ">", tokenStartPos));
            }
        } else {
            tokens.add(new Token(Token.Type.Greater, ">", tokenStartPos));
        }
        currentPos++;
    }

    private void handleLessToken(List<Token> tokens, int tokenStartPos) {
        if (currentPos + 1 < input.length()) {
            int temp = currentPos;
            if (input.charAt(++temp) == '=') {
                tokens.add(new Token(Token.Type.LessEqual, "<=", tokenStartPos));
                currentPos++;
            } else if (input.charAt(temp) == '>') {
                tokens.add(new Token(Token.Type.NotEqual, "<>", tokenStartPos));
                currentPos++;
            } else {
                tokens.add(new Token(Token.Type.Less, "<", tokenStartPos));
            }
        } else {
            tokens.add(new Token(Token.Type.Less, "<", tokenStartPos));
        }
        currentPos++;
    }

    private void handleEqualsToken(List<Token> tokens, int tokenStartPos) {
        if (currentPos + 1 < input.length()) {
            int temp = currentPos;
            if (input.charAt(++temp) == '=') {
                tokens.add(new Token(Token.Type.Equals, "==", tokenStartPos));
                currentPos++;
            } else {
                tokens.add(new Token(Token.Type.Assign, "=", tokenStartPos));
            }
        } else {
            tokens.add(new Token(Token.Type.Assign, "=", tokenStartPos));
        }
        currentPos++;
    }

    private void handleNumberToken(List<Token> tokens, int tokenStartPos) {
        StringBuilder text = new StringBuilder();
        boolean hasDecimal = false;

        while (currentPos < input.length() && (Character.isDigit(input.charAt(currentPos)) || input.charAt(currentPos) == '.')) {
            char currentChar = input.charAt(currentPos);
            if (currentChar == '.') {
                if (hasDecimal) {
                    throw new RuntimeException("ERROR: Invalid floating-point number format at position " + currentPos);
                }
                hasDecimal = true;
            }
            text.append(currentChar);
            currentPos++;
        }

        tokens.add(new Token(hasDecimal ? Token.Type.NumFloat : Token.Type.Num, text.toString(), tokenStartPos));
    }

    private void handleIdentifierOrKeyword(List<Token> tokens, int tokenStartPos) {
        StringBuilder text = new StringBuilder();
        while (currentPos < input.length() && (Character.isLetterOrDigit(input.charAt(currentPos)) || input.charAt(currentPos) == '_')) {
            text.append(input.charAt(currentPos));
            currentPos++;
        }
        String identifier = text.toString();
        Token.Type type;

        switch (identifier) {
            case "TRUE":
            case "FALSE":
                throw new RuntimeException("ERROR: Invalid boolean literal value '" + identifier
                        + "' at position " + tokenStartPos);
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
            case "BREAK":
                type = Token.Type.Break;
                break;
            case "CONTINUE":
                type = Token.Type.Continue;
                break;
            case "BEGIN":
                if (currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 4 < input.length() && input.startsWith("CODE", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.BeginContainer, "BEGIN CODE", tokenStartPos));
                    currentPos += 5;
                } else if(currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 2 < input.length() && input.startsWith("IF", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.BeginIf, "BEGIN IF", tokenStartPos));
                    currentPos += 3;
                } else if(currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 5 < input.length() && input.startsWith("WHILE", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.BeginWhile, "BEGIN WHILE", tokenStartPos));
                    currentPos+=6;
                } else if(currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 3 < input.length() && input.startsWith("FOR", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.BeginFor, "BEGIN FOR", tokenStartPos));
                    currentPos+=4;
                }
                return;
            case "END":
                if (currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 2 < input.length() && input.startsWith("CODE", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.EndContainer, "END CODE", tokenStartPos));
                    currentPos += 5;
                } else if(currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 2 < input.length() && input.startsWith("IF", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.EndIf, "END IF", tokenStartPos));
                    currentPos += 3;
                } else if(currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 5 < input.length() && input.startsWith("WHILE", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.EndWhile, "END WHILE", tokenStartPos));
                    currentPos+=6;
                } else if(currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 3 < input.length() && input.startsWith("FOR", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.EndFor, "END FOR", tokenStartPos));
                    currentPos+=4;
                }
                return;
            case "IF":
                type = Token.Type.If;
                break;
            case "ELSE":
                if (currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 2 < input.length() && input.startsWith("IF", currentPos + 1)) {
                    tokens.add(new Token(Token.Type.IfElse, "ELSE IF", tokenStartPos));
                    currentPos += 3;
                    return;
                }
                type = Token.Type.Else;
                break;
            case "WHILE":
                type = Token.Type.While;
                break;
            case "FOR":
                type = Token.Type.For;
                break;
            default:
                if (Character.isLetter(identifier.charAt(0)) || identifier.charAt(0) == '_') {
                    type = Token.Type.Identifier;
                } else {
                    throw new RuntimeException("ERROR: Invalid identifier '" + identifier + "' at position " + tokenStartPos);
                }
        }
        tokens.add(new Token(type, identifier, tokenStartPos));
    }

    private void printTokens(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.println("Token " + i + ": Type = " + token.getType() + ", Text = '" + token.getText() + "', StartPos = " + token.getStartPos());
        }
    }
}