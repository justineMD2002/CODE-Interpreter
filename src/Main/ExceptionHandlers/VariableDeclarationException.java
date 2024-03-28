package Main.ExceptionHandlers;

public class VariableDeclarationException extends Throwable {
    public VariableDeclarationException() {
        super("An error occured when parsing especially " +
                "with regards to variable declarations. " +
                "Please check for anomalies and try again"
                );
    }
}
