package Main.ExceptionHandlers;


public class DisplayException extends Exception {
    private final int lineNumber;
    public DisplayException(String message, int lineNumber) {
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
