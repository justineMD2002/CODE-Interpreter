package Main.ExceptionHandlers;

import Main.Token.Lexer.Parser.Parser;

public class VariableInitializationException extends Exception {
    private final int lineNumber;
    public VariableInitializationException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public VariableInitializationException(String message) {
        super(message);
        this.lineNumber = 0;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at line " + (getLineNumber() + 1);
    }
}
