package Main.Token.Lexer.Parser;

import Main.Token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer {
    private final List<Token> tokens;
    private int currentTokenIndex;
    private final Map<String, Object> symbolTable;

    public SemanticAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.symbolTable = new HashMap<>();
    }

    public void analyze() {
        // Ensure the program starts with 'BEGIN CODE' and ends with 'END CODE'
        if (!tokens.get(currentTokenIndex).getType().equals(Token.Type.BeginContainer) ||
            !tokens.get(currentTokenIndex).getType().equals(Token.Type.EndContainer)) {
            throw new RuntimeException("Program must start with 'BEGIN CODE' and end with 'END CODE'");
        }

        // Start analysis from the second token, skipping 'BEGIN CODE'
        currentTokenIndex = 1;

        while (currentTokenIndex < tokens.size() - 1) { // Skip the last token 'END CODE'
            Token token = tokens.get(currentTokenIndex);

            if (token.getType() == Token.Type.Identifier) {
                handleIdentifier(token);
            } else if (token.getType() == Token.Type.Print) {
                handleDisplay();
            } else if (token.getType() == Token.Type.Concat) {
                handleConcatenation();
            } else if (token.getType() == Token.Type.NewLine) {
                System.out.println();
            } else if (token.getType() == Token.Type.Scan) {
                handleScan("example input"); // Replace "example input" with actual input
            } else if (token.getType() == Token.Type.Plus ||
                       token.getType() == Token.Type.Minus ||
                       token.getType() == Token.Type.Times ||
                       token.getType() == Token.Type.Divide ||
                       token.getType() == Token.Type.Modulo) {
                handleArithmeticOperation(token);
            } else {
                throw new RuntimeException("Unexpected token type: " + token.getType());
            }

            currentTokenIndex++;
        }
    }


    private void handleIdentifier(Token token) {
        // Check for variable declaration and initialization
        if (currentTokenIndex + 1 < tokens.size() && tokens.get(currentTokenIndex + 1).getType() == Token.Type.Assign) {
            String varName = token.getText();
            Token valueToken = tokens.get(currentTokenIndex + 2);
            Object value = parseValue(valueToken);
            symbolTable.put(varName, value);
            currentTokenIndex += 2; // Skip the assignment and value tokens
        }
    }

    private void handleDisplay() {
        if (currentTokenIndex + 1 < tokens.size()) {
            Token nextToken = tokens.get(currentTokenIndex + 1);
            if (nextToken.getType() == Token.Type.Identifier && symbolTable.containsKey(nextToken.getText())) {
                System.out.print(symbolTable.get(nextToken.getText()));
            } else if (nextToken.getType() == Token.Type.CharLiteral) {
                String text = nextToken.getText();
                if (text.startsWith("[") && text.endsWith("]")) {
                    // Handle escape code
                    System.out.print(text.substring(1, text.length() - 1));
                } else {
                    // Print without quotes
                    System.out.print(text.substring(1, text.length() - 1));
                }
            } else {
                System.out.print(nextToken.getText());
            }
            currentTokenIndex++; // Skip the next token
        }
    }
    

    private void handleConcatenation() {
        if (currentTokenIndex - 1 >= 0 && currentTokenIndex + 1 < tokens.size()) {
            Token leftToken = tokens.get(currentTokenIndex - 1);
            Token rightToken = tokens.get(currentTokenIndex + 1);
            String leftValue = leftToken.getText();
            String rightValue = rightToken.getText();
            if (leftToken.getType() == Token.Type.Identifier && symbolTable.containsKey(leftValue)) {
                leftValue = symbolTable.get(leftValue).toString();
            }
            if (rightToken.getType() == Token.Type.Identifier && symbolTable.containsKey(rightValue)) {
                rightValue = symbolTable.get(rightValue).toString();
            }
            System.out.print(leftValue + rightValue);
            currentTokenIndex += 2; // Skip the concatenation and right operand tokens
        }
    }

    private void handleScan(String input) {
        if (currentTokenIndex + 1 < tokens.size()) {
            Token nextToken = tokens.get(currentTokenIndex + 1);
            if (nextToken.getType() == Token.Type.Identifier) {
                symbolTable.put(nextToken.getText(), input);
                currentTokenIndex++; // Skip the identifier token
            }
        }
    }

    private void handleArithmeticOperation(Token token) {
        if (currentTokenIndex - 1 >= 0 && currentTokenIndex + 1 < tokens.size()) {
            Token leftToken = tokens.get(currentTokenIndex - 1);
            Token rightToken = tokens.get(currentTokenIndex + 1);
            int leftValue = getValue(leftToken);
            int rightValue = getValue(rightToken);
            int result = 0;
            switch (token.getType()) {
                case Plus:
                    result = leftValue + rightValue;
                    break;
                case Minus:
                    result = leftValue - rightValue;
                    break;
                case Times:
                    result = leftValue * rightValue;
                    break;
                case Divide:
                    result = leftValue / rightValue;
                    break;
                case Modulo:
                    result = leftValue % rightValue;
                    break;
                default:
                    break;
            }
            System.out.print(result);
            currentTokenIndex += 2; // Skip the operator and right operand tokens
        }
    }

    private int getValue(Token token) {
        if (token.getType() == Token.Type.Identifier) {
            String varName = token.getText();
            if (symbolTable.containsKey(varName)) {
                Object value = symbolTable.get(varName);
                if (value instanceof Integer) {
                    return (int) value;
                } else {
                    throw new RuntimeException("Variable '" + varName + "' does not hold an integer value");
                }
            } else {
                throw new RuntimeException("Undefined variable: " + varName);
            }
        } else if (token.getType() == Token.Type.Num) {
            return Integer.parseInt(token.getText());
        } else {
            throw new RuntimeException("Unsupported token type for arithmetic operation: " + token.getType());
        }
    }

    private Object parseValue(Token valueToken) {
        switch (valueToken.getType()) {
            case Num:
                return Integer.parseInt(valueToken.getText());
            case CharLiteral:
                return valueToken.getText().substring(1, valueToken.getText().length() - 1); // Remove quotes
            case Identifier:
                // Check if the identifier exists in the symbol table
                if (symbolTable.containsKey(valueToken.getText())) {
                    return symbolTable.get(valueToken.getText());
                } else {
                    throw new RuntimeException("Undefined variable: " + valueToken.getText());
                }
            // Add cases for other value types as needed
            default:
                throw new RuntimeException("Unsupported value type: " + valueToken.getType());
        }
    }
}