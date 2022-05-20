package parser.lr0;

import parser.util.Rule;

import java.util.Arrays;
import java.util.Objects;

public class LR0Item extends Rule {

    protected int dotPointer; //点的位置

    public LR0Item(Rule r) {
        super(r.getLeftSide(), r.getRightSide());
        int finished = 0;
        if (r.getRightSide().length == 1 && r.getRightSide()[0].equals("epsilon")) {
            finished = 1;
        }
        this.dotPointer = finished;
    }

    public LR0Item(String leftSide, String[] rightSide, int dotPointer) {
        super(leftSide, rightSide);
        this.dotPointer = dotPointer;
    }

    public LR0Item(LR0Item item) {
        super(item);
        dotPointer = item.getDotPointer();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.dotPointer;
        hash = 89 * hash + Objects.hashCode(this.leftSide);
        hash = 89 * hash + Arrays.deepHashCode(this.rightSide);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LR0Item other = (LR0Item) obj;
        if (this.dotPointer != other.dotPointer) {
            return false;
        }
        if (!this.leftSide.equals(other.leftSide)) {
            return false;
        }
        if (!Arrays.equals(this.rightSide, other.rightSide)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = leftSide + " -> ";
        // 规则右部加上 "点"
        for (int i = 0; i < rightSide.length; i++) {
            if (i == dotPointer) {
                str += ".";
            }
            str += rightSide[i];
            if(i != rightSide.length - 1){
                str+= " "; //词法单元间用空格分隔
            }
        }
        if (rightSide.length == dotPointer) {
                str += ".";
        }
        return str;
    }
    

    public int getDotPointer() {
        return dotPointer;
    }

    /**
     * "点"的位置右移一位
     */
    boolean goTo() {
        if (dotPointer >= rightSide.length) {
            return false;
        }
        dotPointer++;
        return true;
    }

    /**
     * 获取紧随 "点" 之后的词法单元
     */
    String getCurrentTerminal() {
        if(dotPointer == rightSide.length){
            return null;
        }
        //事实上rightSide没有存 "点"，所以 dotPointer 就是"点"之后的符号
        return rightSide[dotPointer];
    }

}
