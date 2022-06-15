package lexer;

public class Real extends Token {
    public final float value;

    public Real(float v) {
        super(Tag.REAL);
        value = v;
    }

    @Override
    public String first() {
        return "real";
    }

    @Override
    public String second() {
        return "" + value;
    }
}
