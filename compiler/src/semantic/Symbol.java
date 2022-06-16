package semantic;

import java.util.ArrayList;

//符号栈中的符号封装 参考ppt 5c结尾
public class Symbol {
    private String first; // first second 对应 词法单元 <first, second> 如 <id, x>
    private String second; // 词法单元符号的值（id的lexeme，num的value）
    private String addr; // 用于指示一个标识符(临时变量？)
    private ArrayList<Integer> trueList;    //用arraylist充当数据结构  存指令的行号（后续backpatch填goto的标号到这些行）
    private ArrayList<Integer> falseList;
    private int instr; // 用于占位符M记住指令标号
    private ArrayList<Integer> nextList; //控制流语句，带有一个未填充的跳转链，

    public Symbol() { }

    public Symbol(String first, String second, String addr, ArrayList<Integer> trueList, ArrayList<Integer> falseList, int instr, ArrayList<Integer> nextList){
        this.first = first;
        this.second = second;
        this.addr = addr;
        this.trueList = trueList;
        this.falseList = falseList;
        this.instr = instr;
        this.nextList = nextList;
    }

    public Symbol(String first, String second, String addr, ArrayList<Integer> trueList, ArrayList<Integer> falseList, int instr){
        this(first, second, addr, trueList, falseList, instr, null);
    }

    public Symbol(String first, String second, String addr, ArrayList<Integer> trueList, ArrayList<Integer> falseList) {
        this(first, second, addr, trueList, falseList, -1, null);
    }

    public Symbol(String first, String second, String addr, ArrayList<Integer> trueList){
        this(first, second, addr, trueList, null, -1, null);
    }

    public Symbol(String first, String second, String addr){
        this(first, second, addr, null, null, -1, null);
    }

    public String getFirst() {
        return first;
    }

    public Symbol setFirst(String first) {
        this.first = first;
        return this;
    }

    public String getSecond() {
        return second;
    }

    public Symbol setSecond(String second) {
        this.second = second;
        return this;
    }

    public String getAddr() {
        return addr;
    }

    public Symbol setAddr(String addr) {
        this.addr = addr;
        return this;
    }

    public ArrayList<Integer> getTrueList() {
        return trueList;
    }

    public Symbol setTrueList(ArrayList<Integer> trueList) {
        this.trueList = trueList;
        return this;
    }

    public ArrayList<Integer> getFalseList() {
        return falseList;
    }

    public Symbol setFalseList(ArrayList<Integer> falseList) {
        this.falseList = falseList;
        return this;
    }

    public int getInstr() {
        return instr;
    }

    public Symbol setInstr(int instr) {
        this.instr = instr;
        return this;
    }

    public ArrayList<Integer> getNextList() {
        return nextList;
    }

    public Symbol setNextList(ArrayList<Integer> nextList) {
        this.nextList = nextList;
        return this;
    }
}
