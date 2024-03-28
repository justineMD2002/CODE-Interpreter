package Main;

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
            if(parser.parse()) {
                System.out.println("Parsing complete");
            } else {
                System.out.println("There was an error parsing your code");
            }
        }
        scan.close();
    }
}
