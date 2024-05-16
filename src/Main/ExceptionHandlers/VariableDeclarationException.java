package Main.ExceptionHandlers;


public class VariableDeclarationException extends Exception {
    private final int lineNumber;
    public VariableDeclarationException(String message, int lineNumber) {
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
