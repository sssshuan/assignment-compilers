package symbols;

public class Info {
    Type type;
    boolean ns;

    public Info(Type type, boolean ns) {
        this.type = type;
        this.ns = ns;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isNs() {
        return ns;
    }

    public void setNs(boolean ns) {
        this.ns = ns;
    }
}