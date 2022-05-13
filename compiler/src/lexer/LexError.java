package lexer;

public class LexError extends Token{

    private final int line;

    private final String msg;

    /**
     * @param line 行数
     * @param msg 错误信息
     */
    public LexError(int line, String msg) {
        super(Tag.ERROR);
        this.line = line;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "line " + line + ", Error: " + msg;
    }

}
