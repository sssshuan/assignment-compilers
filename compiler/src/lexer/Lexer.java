package lexer;

import symbols.Type;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

public class Lexer {

    private String sourceCode = "";
    private int currentIndex = 0;

    public static int line = 1;
    char peek = ' ';
    Hashtable words = new Hashtable();

    void reserve(Word w) {
        words.put(w.lexeme, w);
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
        this.sourceCode = source;
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
            if(currentIndex == sourceCode.length()) {
                return new Token(-1); //结束标志
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
            if (peek != '.')
                //判断进制
                // 0.123 0123  0x123 几种情况
                if(peek == '0') {
                    readch();
                    if(peek == 'x') {
                        radix = 16;
                        readch();
                    }else if(peek != '.'){
                        radix = 8;
                    }
                }
                //转成整数
                while (Character.isDigit(peek) || (radix == 16 && (peek >= 'a' && peek <= 'f')) ) {
                    v = radix * v + Character.digit(peek, radix);
                    readch();
                }
            if (peek != '.') return new Num(v);
            //在作为float之前先判断 ..
            //遇到 .. 直接返回前面的整数  ..留到下一轮在复合词法单元部分识别
            if(sourceCode.charAt(currentIndex) == '.') {
                return new Num(v);
            }

            // 浮点数保证十进制
            if(radix != 10) {
                //错误
            }

            float x = v, d = 10;
            for (; ; ) {
                readch();
                if (!Character.isDigit(peek)) break;
                x = x + Character.digit(peek, 10) / d;
                d = d * 10;
            }
            return new Real(x);
        }
        if (Character.isLetter(peek) || peek == '_') {
            StringBuffer b = new StringBuffer();
            do {
                b.append(peek);
                readch();
            } while (Character.isLetterOrDigit(peek) || peek == '_');
            String s = b.toString();
            Word w = (Word) words.get(s);
            if (w != null) return w;
            w = new Word(s, Tag.ID); // 标识符 抽象成 id
            words.put(s, w);
            return w;
        }
        Token tok = new Token(peek); // 任意字符  值直接作为 tag
        peek = ' ';
        return tok;
    }
}
