import lexer.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream("code.txt");
        String code = new String(inputStream.readAllBytes());

        Lexer lexer = new Lexer();


        StringBuilder builder = new StringBuilder();

        while (true) {
            Token token = lexer.scan();
            if (token instanceof Word) {
                Word word = (Word) token;
                //暂时用于退出循环
                if (((Word) token).lexeme.equals("shenhuan")) {
                    break;
                }
                //如果是标识符 加个id， 否则是保留字，直接输出
                builder.append("<" + (word.tag == Tag.ID ? "id, " : "") + word.lexeme + ">");
            } else if (token instanceof Num) {
                builder.append("<num, " + ((Num) token).value + ">");
            } else if (token instanceof Real) {
                builder.append("<real, " + ((Real) token).value + ">");
            } else {
                builder.append("<" + (char) token.tag + ">");
            }
//            Logger.getGlobal().fine("11");
        }

        System.out.println(builder);

        test(builder.toString());


    }

//
//    /**
//     * 删除注释、把除了字符串里的字符 统一转成大写
//     *
//     * @param input 程序代码
//     * @return 返回处理好的字符串
//     */
//    public static String preProcess(String input) {
//        StringBuilder stringBuilder = new StringBuilder("  " + input + "  ");
//
//        // 删除注释、把除了字符串里的字符 统一转成大写
//        for (int i = 0; i < stringBuilder.length(); i++) {
//            //判断是否遇到注释行
//            if (stringBuilder.charAt(i) == '/' && stringBuilder.charAt(i + 1) == '/') {
//                int j = stringBuilder.indexOf("\n", i); //找出本行结束位置
//                stringBuilder.delete(i, j); //去除注释
//            } else if (stringBuilder.charAt(i) == '"') {
//                //找出对应的引号所在位置, 赋值给i跳过字符串的大小写处理
//                i = stringBuilder.indexOf("\"", i + 1);
//            } else { //转换为大写字符
//                //要大写还是小写 看lexer默认加的关键词
//                stringBuilder.setCharAt(i, Character.toLowerCase(stringBuilder.charAt(i)));
//            }
//        }
//
//        return stringBuilder.toString();
//    }

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
