package Main.ExceptionHandlers;

public class SyntaxErrorException extends Exception {
    private final int lineNumber;
    public SyntaxErrorException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public SyntaxErrorException(String message) {
        super(message);
        this.lineNumber = 0;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        if(getLineNumber() > 0)
            return super.getMessage() + " at line " + (getLineNumber() + 1) ;
        else
            return super.getMessage();
    }
}
