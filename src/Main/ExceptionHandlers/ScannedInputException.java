package Main.ExceptionHandlers;

public class ScannedInputException extends Exception{
    private final int lineNumber;
    public ScannedInputException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at line " + (getLineNumber()+1);
    }
}
