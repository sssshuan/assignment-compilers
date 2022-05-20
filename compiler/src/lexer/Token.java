package lexer;

public class Token {
    public final int tag;

    public Token(int t) {
        tag = t;
    }

    /**
     *
     */
    public String toString() {
        return "<" + (char) tag + ">";
    }

    /**
     * 记号（在语法分析中的模样）
     */
    public String desc() {
        return (char)tag + "";
    }
}
