import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("Enter code:");
            String code = scan.nextLine();
            Lexer lexer = new Lexer(code);
            lexer.lex();
        }
    }
}
