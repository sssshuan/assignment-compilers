package lexer;

public class Word extends Token {
    public String lexeme = "";

    public Word(String s, int tag) {
        super(tag);
        lexeme = s;
    }

    public String toString() {
        return "<" + (tag == Tag.ID ? "id, " : "") + lexeme + ">";
    }

    @Override
    public String desc() {
        return tag == Tag.ID ? "id" : lexeme;
    }

    public static final Word
            and = new Word("&&", Tag.AND), or = new Word("||", Tag.OR),
            eq = new Word("==", Tag.EQ), ne = new Word("!=", Tag.NE),
            le = new Word("<=", Tag.LE), ge = new Word(">=", Tag.GE),
//            range = new Word("..", Tag.RANGE),
//            minus = new Word("minus", Tag.MINUS),
            True = new Word("true", Tag.TRUE),
            False = new Word("false", Tag.FALSE),
//            temp = new Word("t", Tag.TEMP),
            shiftLeft = new Word("<<", Tag.SHIFT_LEFT), shiftRight = new Word(">>", Tag.SHIFT_RIGHT),
            auto_decrement = new Word("--", Tag.AUTO_DECREMENT),
            auto_increment = new Word("++", Tag.AUTO_INCREMENT),
            assignment = new Word(":=", Tag.ASSIGNMENT),
            minus_assign = new Word("-=", Tag.MINUS_ASSIGN),
            plus_assign = new Word("+=", Tag.PLUS_ASSIGN),
            multiply_assign = new Word("*=", Tag.MULTIPLY_ASSIGN),
            divide_assign = new Word("/=", Tag.DIVIDE_ASSIGN),
            range =  new Word("..", Tag.RANGE);
}
