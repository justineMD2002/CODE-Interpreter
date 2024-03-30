package Main;

import Main.Nodes.ASTNode;
import Main.Nodes.ProgramNode;
import Main.Token.Lexer.Lexer;
import Main.Token.Lexer.Parser.Parser;
import Main.Token.Token;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("Enter code:");
            StringBuilder builder = new StringBuilder();
            if(builder.toString().equals("exit")) break;
            while (true) {
                String code = scan.nextLine();

                builder.append(code).append('\n');
                if (code.equalsIgnoreCase("END CODE")) {
                    break; // Exit the loop if 'exit' is entered
                }
            }
            Lexer lexer = new Lexer(builder.toString());
            List<Token> tokens = lexer.lex();
            Parser parser = new Parser(tokens);
            try {
                ASTNode parsedNode = parser.parse();
                if (parsedNode instanceof ProgramNode programNode) {
                    programNode.displayOutput();
                }
                System.out.println("Parsing complete\n\n");
            } catch (Exception e) {
                System.err.println("Error parsing code:");
                e.printStackTrace();
            }
        }
        scan.close();
    }
}
