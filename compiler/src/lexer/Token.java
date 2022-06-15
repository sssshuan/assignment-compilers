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
        if(second() == null) {
            return "<" + first() + ">";
        } else {
            return "<" + first() + ", " + second() + ">";
        }
    }

    /**
     * 记号（在语法分析中的模样）
     */
    public String first() {
        return (char)tag + "";
    }

    /**
     * 词法值 如 id.lexeme, num.value
     */
    public String second() {
        return null;
    }
}
