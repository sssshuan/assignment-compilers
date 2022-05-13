import lexer.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream("code.txt");
        String code = new String(inputStream.readAllBytes());

        Lexer lexer = new Lexer(code);

        // 开始分割词法单元
        StringBuilder builder = new StringBuilder();
        while (true) {
            Token token = lexer.scan();
            if (token instanceof Word) {
                Word word = (Word) token;
                //如果是标识符 加个id， 否则是保留字，直接输出
                builder.append("<" + (word.tag == Tag.ID ? "id, " : "") + word.lexeme + ">");
            } else if (token instanceof Num) {
                builder.append("<num, " + ((Num) token).value + ">");
            } else if (token instanceof Real) {
                builder.append("<real, " + ((Real) token).value + ">");
            } else {
                if(token.tag == Tag.CODE_END) {break;} //作为结束标志
                if(token.tag != Tag.ERROR) {
                    builder.append("<" + (char) token.tag + ">");
                }
            }
        }

        // 错误信息
        for(LexError error : lexer.getErrors()) {
            Logger.getGlobal().severe(error.toString());
        }

        System.out.println(builder);

        test(builder.toString());

    }

    /**
     * 测试
     * @param str
     * @throws IOException
     */
    public static void test(String str) throws IOException {
        FileInputStream inputStream = new FileInputStream("ans.txt");
        String ans = new String(inputStream.readAllBytes());

        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) != ans.charAt(i)) {
                Logger.getGlobal().severe(str.substring(i, i + 10));
                break;
            }
        }
    }


}
