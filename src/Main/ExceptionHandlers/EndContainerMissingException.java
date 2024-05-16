package Main.ExceptionHandlers;

import Main.Token.Lexer.Parser.Parser;

public class EndContainerMissingException extends Exception {
    private final int lineNumber;
    public EndContainerMissingException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at line " + (getLineNumber() + 1);
    }
}
