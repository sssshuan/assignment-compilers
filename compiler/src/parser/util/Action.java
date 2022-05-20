package parser.util;

public class Action {
    // 操作类型
    private ActionType type;
    // 操作数（对于移入操作是状态、对于归约操作是第几条规则）
    private int operand;

    public Action(ActionType type, int operand) {
        this.type = type;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return type + " " + (type == ActionType.ACCEPT ? "":operand);
    }

    public ActionType getType() {
        return type;
    }

    public int getOperand() {
        return operand;
    }
    
}
