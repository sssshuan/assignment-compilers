package lexer;

import symbols.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    private String sourceCode = "";
    private int currentIndex = 0;

    public static int line = 1;
    char peek = ' ';
    // 用于存放 保留字、标识符
    Hashtable<String, Word> words = new Hashtable<>();
    public List<Word> wordList = new ArrayList<>(); //顺序保存
    // 用于存放错误
    public List<LexError> errors = new ArrayList<>();

    void reserve(Word w) {
        words.put(w.lexeme, w);
        wordList.add(w); // 保存一份顺序的
    }

    public Lexer(String source) {
        reserve(new Word("if", Tag.IF));
        reserve(new Word("else", Tag.ELSE));
        reserve(new Word("while", Tag.WHILE));
        reserve(new Word("do", Tag.DO));
        reserve(new Word("break", Tag.BREAK));
        reserve(new Word("continue", Tag.CONTINUE));
        reserve(new Word("for", Tag.FOR));
        reserve(new Word("in", Tag.IN));
        reserve(Word.True);
        reserve(Word.False);
        reserve(Type.Int);
        reserve(Type.Char);
        reserve(Type.Bool);
        reserve(Type.Float);
        // 预处理代码
        preProcess(source);
        line = 1;
    }

    void readch() throws IOException {
        peek = sourceCode.charAt(currentIndex++);//(char) System.in.read();
    }

    boolean readch(char c) throws IOException {
        readch();
        if (peek != c) return false;
        peek = ' '; // 如果和预期相同，就把peek清空， 否则把这个字符留着 在下一个单元
        return true;
    }

    public Token scan() throws IOException {
        //空白
        for (; ; readch()) {
            if (currentIndex == sourceCode.length()) {
                return new Token(Tag.CODE_END); //结束标志
            }
            if (peek == ' ' || peek == '\t') continue;
            else if (peek == '\n') line = line + 1;
            else break;
        }
        // 复合词法单元
        switch (peek) {
            case '&':
                if (readch('&')) return Word.and;
                else return new Token('&');
            case '|':
                if (readch('|')) return Word.or;
                else return new Token('|');
            case '=':
                if (readch('=')) return Word.eq;
                else return new Token('=');
            case '!':
                if (readch('=')) return Word.ne;
                else return new Token('!');
            case '<':
                if (readch('=')) return Word.le;
                else if (peek == '<') { peek = ' ' ; return Word.shiftLeft; } //匹配成功要把peek清掉 否则会被留着下一次匹配
                else return new Token('<');
            case '>':
                if (readch('=')) return Word.ge;
                else if(peek == '>') { peek = ' ' ; return Word.shiftRight; }
                else return new Token('>');
            case ':':
                if (readch('=')) return Word.assignment;
                else return new Token(':'); //这边应该错误？
            case '-':
                if (readch('-')) return Word.auto_decrement;
                else if(peek == '=') { peek = ' ' ; return Word.minus_assign; }
                else return new Token('-');
            case '+':
                if (readch('+')) return Word.auto_increment;
                else if(peek == '=') { peek = ' ' ; return Word.plus_assign; }
                else return new Token('+');
            case '*':
                if (readch('=')) return Word.multiply_assign;
                else return new Token('*');
            case '/':
                if (readch('=')) return Word.divide_assign;
                else return new Token('/');
            case '.':
                if (readch('.')) return Word.range;
        }

        //数字
        if (Character.isDigit(peek) || peek == '.') {
            int v = 0;
            int radix = 10; //进制
            if (peek != '.') {
                //判断进制
                // 0.123 0123  0x123 几种情况
                if (peek == '0') {
                    readch();
                    if (peek == 'x') {
                        radix = 16;
                        readch();
                    } else if (peek != '.') {
                        radix = 8;
                    }
                }
            }
            //转成整数
            while (Character.digit(peek, radix) >= 0 && Character.digit(peek, radix) < radix) {
                if (v > 2147483647 / radix) { //马上越界
                    forwardUtilTokenEnd(); // 越界了也继续读，把该单元读完
                    return addError(line, "整数越界");
                }
                v = radix * v + Character.digit(peek, radix);
                readch();
            }
            //读完整数了
            if (peek != '.') {
                //整数后面接字母、数字、下划线、非法字符，错误
                boolean radix8Error = radix == 8 && Character.digit(peek, 10) > 8; //8进制 8、9两个数字特殊
                if(Character.isLetter(peek)  || peek == '_' || characterIllegal(peek) || radix8Error) {
                    //把数值换回原表示，用于错误提示
                    String prefix;
                    switch (radix) {
                        case 8 -> prefix = "0" + Integer.toOctalString(v);
                        case 16 -> prefix = "0x" + Integer.toHexString(v);
                        default -> prefix = String.valueOf(v);
                    }
                    String token = prefix + forwardUtilTokenEnd();
                    return addError(line, "'" + token + "' 不合法");
                }
                //正常的话返回整数
                return new Num(v);
            }
            //程序走到这边 peek一定是'.'   在作为float之前先判断 ..
            //遇到 .. 直接返回前面的整数  ..留到下一轮在复合词法单元部分识别
            if (sourceCode.charAt(currentIndex) == '.') {
                return new Num(v);
            }

            // 浮点数保证十进制
            if (radix != 10) {
                return addError(line, "数值错误");
            }
            // .之后没有数字的情况 比如 3. + 5
            readch();
            if(!Character.isDigit(peek)) {
                String token = String.valueOf(v) + '.';
                return addError(line, "'" + token + "' 不合法");
            }
            // 为了避免浮点数加减乘除出现误差，用字符串解析成float
            StringBuilder floatStringBuilder = new StringBuilder(v + ".");
            while (Character.isDigit(peek)) {
                floatStringBuilder.append(peek);
                readch();
            }
            //和整数部分一样 浮点数后如果连着字母、下划线、非法字符 同样报错
            if(Character.isLetter(peek)  || peek == '_' || characterIllegal(peek)) {
                String token = floatStringBuilder + forwardUtilTokenEnd();
                return addError(line, "'" + token + "' 不合法");
            }
            return new Real(Float.parseFloat(floatStringBuilder.toString()));
        }
        //判断是否标识符：字母或下划线开头
        if (Character.isLetter(peek) || peek == '_') {
            StringBuilder b = new StringBuilder();
            do {
                b.append(peek);
                readch();
            } while (Character.isLetterOrDigit(peek) || peek == '_');
            //有非法字符 不合法
            if(characterIllegal(peek)) {
                //把当前单元读完
                String token = b + forwardUtilTokenEnd();
                return addError(line, "'" + token + "' 不合法");
            }
            //合法标识符
            String s = b.toString();
            Word w = words.get(s);
            if (w != null) return w;
            w = new Word(s, Tag.ID); // 标识符 抽象成 id
            reserve(w);
            return w;
        }
        if (characterIllegal(peek)) {
            LexError error = addError(line, "非法字符 '" + peek + "'");
            peek = ' ';
            return error;
        }
        Token tok = new Token(peek); // 任意字符  值直接作为 tag
        peek = ' ';
        return tok;
    }


    /**
     * 删除注释、把除了字符串里的字符 统一转成小写
     */
    private void preProcess(String input) {
        StringBuilder stringBuilder = new StringBuilder(input);
        // 删除注释、把除了字符串里的字符 统一转成大写
        for (int i = 0; i < stringBuilder.length(); i++) {
            //判断是否遇到注释行
            if (stringBuilder.charAt(i) == '/' && stringBuilder.charAt(i + 1) == '/') {
                int j = stringBuilder.indexOf("\n", i); //找出本行结束位置
                stringBuilder.delete(i, j); //去除注释
            } else if (stringBuilder.charAt(i) == '"') {
                //找出对应的引号所在位置, 赋值给i跳过字符串的大小写处理
                i = stringBuilder.indexOf("\"", i + 1);
            }else if(stringBuilder.charAt(i) =='\r'){
                //将\r字符去掉(Mac和windows系统存在不同，mac里回车是\n,windows里是\n\r)
                stringBuilder.delete(i,i+1);
                i--;
            }
            else { //转换为小写字符
                //要大写还是小写 看lexer默认加的关键词
                stringBuilder.setCharAt(i, Character.toLowerCase(stringBuilder.charAt(i)));
            }
        }
        sourceCode = stringBuilder.toString();
    }


    /**
     * 判断字符是否非法
     * @param c 要判断的字符
     * @return 非法返回true
     */
    private boolean characterIllegal(char c) {
        String pattern = "[a-z0-9\\+\\*/\\\\=\\&\\|\\!\\?\\>\\<\\:\\.\\,\\(\\)\\;\\{\\}\\[\\]\\^\\%\\_\\\t\\n\\s-]";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher("" + c);
        return !matcher.find();
    }


    /**
     * 当出现错误时，把该单元读完
     * @return 返回该单元剩余部分
     */
    private String forwardUtilTokenEnd() throws IOException {
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(peek);
            readch();
        }while (Character.isLetterOrDigit(peek) || peek =='_' || characterIllegal(peek));

        return builder.toString();
    }

    /**
     * 添加一个错误
     * @param line 所在行数
     * @param msg 错误信息
     * @return 返回新加的错误
     */
    private LexError addError(int line, String msg) {
        errors.add(new LexError(line, msg));
        return errors.get(errors.size()-1);
    }

    /**
     * 输出符号表
     */
    public void printWordTable() {
        for(Word word : wordList) {
            System.out.println(word.lexeme);
        }
    }

    /**
     * 输出全部错误
     */
    public void printErrors() {
        for(LexError error : errors) {
            System.out.println(error);
        }
    }

}
