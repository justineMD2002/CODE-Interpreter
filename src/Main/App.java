package Main;

import Main.Nodes.ASTNode;
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

            Lexer lexer = new Lexer(fileContent.toString());
            List<Token> tokens = lexer.lex();
            Parser parser = new Parser(tokens);
            ASTNode parsedNode = parser.parse();
            if (parsedNode instanceof ProgramNode programNode) {
                programNode.displayOutput();
            }
            System.out.println("Parsing complete");
        } catch (IOException e) {
            System.err.println("Error reading file:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error parsing code:");
            e.printStackTrace();
        }
    }
}
