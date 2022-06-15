package semantic;

import java.util.ArrayList;

public class Symbol {
    private String first;
    private String second;
    private String addr;
    private ArrayList<Integer> trueList;    //用arraylist充当数据结构
    private ArrayList<Integer> falseList;
    private int instr; //指令标号？
    private ArrayList<Integer> nextList; //

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
