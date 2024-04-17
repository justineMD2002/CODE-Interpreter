package Main;

import Main.ExceptionHandlers.*;
import Main.Nodes.ProgramNode;
import Main.Token.Lexer.Lexer;
import Main.Token.Lexer.Parser.Parser;
import Main.Token.Token;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/Main/main.code"))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            int errorCount = getErrorCount(fileContent);
            if (errorCount == 0) {
                System.out.println("No errors found in CODE.");
            }
        } catch (IOException e) {
            System.err.println("Error reading file:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static int getErrorCount(StringBuilder fileContent) throws BeginContainerMissingException, EndContainerMissingException, VariableInitializationException, DisplayException, VariableDeclarationException {
        Lexer lexer = new Lexer(fileContent.toString());
        List<Token> tokens = lexer.lex();
        int errorCount = 0;
        while (errorCount == 0 && !tokens.isEmpty()) {
            Parser parser = new Parser(tokens);
            ProgramNode program = (ProgramNode) parser.parse();
            List<Token> sublistToRemove = tokens.subList(0, parser.getCurrentTokenIndex());
            tokens.removeAll(sublistToRemove);
            errorCount = parser.getErrorCount();
        };
        return errorCount;
    }
}
