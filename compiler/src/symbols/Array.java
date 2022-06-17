package symbols;

import lexer.Tag;

public class Array extends Type {
    public Type element;
    public int size = 1;

    public Array(int sz, Type p) {
        super("[]", Tag.INDEX, sz * p.width);
        size = sz;
        element = p;
    }

    public String toString() {
        return "[" + size + "]" + element.toString();
    }
}
