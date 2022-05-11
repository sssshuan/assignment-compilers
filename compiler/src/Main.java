import lexer.Lexer;
import lexer.Token;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream("code.txt");
        String code = new String(inputStream.readAllBytes());

        Lexer lexer = new Lexer();


        while (true) {
            lexer.scan();
            Logger.getGlobal().fine("11");
        }

//        // write your code here
//        Lexer lex = new Lexer();
//        Parser parse = new Parser(lex);
//        parse.program();
//        System.out.write('\n');

    }
}
