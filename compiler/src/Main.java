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
            if (token.tag == Tag.CODE_END) {
                break;
            } else if (token.tag != Tag.ERROR) {
                builder.append(token);
            }
        }

        System.out.println("分析结果:");
        System.out.println(builder);

        System.out.println("\n\n\n");

        System.out.println("错误信息：");
        lexer.printErrors();

        System.out.println("\n\n\n");

        System.out.println("符号表：");
        lexer.printWordTable();

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
