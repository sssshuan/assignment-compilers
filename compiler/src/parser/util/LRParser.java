package parser.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public abstract class LRParser {

    //在数组中的索引 与 状态 对应
    //键为非终结符，值为要移入的状态
    protected HashMap<String, Integer>[] goToTable;
    //在数组中的索引 与 状态 对应
    //键为终结符，值为操作
    protected HashMap<String, Action>[] actionTable;
    protected Grammar grammar;

    //


    public LRParser(Grammar grammar) {
        this.grammar = grammar;
    }

    protected abstract void createGoToTable();

    /**
     * 分析过程
     */
    public boolean accept(ArrayList<String> inputs) {
        inputs.add("$");
        int inputIndex = 0;
        Stack<String> stack = new Stack<>(); //符号栈
        Stack<Integer> stateStack = new Stack<>(); //状态栈

        stateStack.push(0);
        stack.push("$");

        while(inputIndex < inputs.size()){
            int state = stateStack.peek();
            String nextInput = inputs.get(inputIndex);
            Action action = actionTable[state].get(nextInput);
            if(action == null){
                // 表项为空
                return false;
            }else if(action.getType() == ActionType.SHIFT){
                //移入
                stack.push(nextInput);
                stateStack.push(action.getOperand());

//for(int i = 0; i < stack.size(); ++i) {
//    System.out.print(stack.get(i));
//}
//System.out.println();

                inputIndex++;
            }else if(action.getType() == ActionType.REDUCE){
                //归约
                int ruleIndex = action.getOperand();
                Rule rule = grammar.getRules().get(ruleIndex);
                String leftSide = rule.getLeftSide();
                int rightSideLength = rule.getRightSide().length;
                if(rightSideLength == 1 && rule.getRightSide()[0].equals("epsilon")) {
                    rightSideLength = 0;
                }

                for(int i=0; i < rightSideLength ; i++){
                    stack.pop();
                    stateStack.pop();
                }

                int nextState = stateStack.peek();
                stack.push(leftSide);
                int variableState = goToTable[nextState].get(leftSide);
                stateStack.push(variableState);

//for(int i = 0; i < stack.size(); ++i) {
//    System.out.print(stack.get(i));
//}
//System.out.print("\treduce ");
//System.out.print(rule);
//
//System.out.println();

            }else if(action.getType() == ActionType.ACCEPT){
                //接受
                System.out.println("accept");
                return true;
            }
        }
        return false;
    }

    /**
     * 分析表的非终结符集部分
     */
    public String goToTableStr() {
        String str = "Go TO Table : \n";
        str += "          ";
        for (String variable : grammar.getVariables()) {
            str += String.format("%-6s",variable);
        }
        str += "\n";

        for (int i = 0; i < goToTable.length; i++) {
            for (int j = 0; j < (grammar.getVariables().size()+1)*6+2; j++) {
                str += "-";
            }
            str += "\n";
            str += String.format("|%-6s|",i);
            for (String variable : grammar.getVariables()) {
                str += String.format("%6s",(goToTable[i].get(variable) == null ? "|" : goToTable[i].get(variable)+"|"));
            }
            str += "\n";
        }
        for (int j = 0; j < (grammar.getVariables().size()+1)*6+2; j++) {
            str += "-";
        }
        return str;
    }

    /**
     * 分析表的终结符集部分
     */
    public String actionTableStr() {
        String str = "Action Table : \n";
        HashSet<String> terminals = new HashSet<>(grammar.getTerminals());
        terminals.add("$");
        str += "                ";
        for (String terminal : terminals) {
            str += String.format("%-10s" , terminal);
        }
        str += "\n";

        for (int i = 0; i < actionTable.length; i++) {
            for (int j = 0; j < (terminals.size()+1)*10+2; j++) {
                str += "-";
            }
            str += "\n";
            str += String.format("|%-10s|",i);
            for (String terminal : terminals) {
                str += String.format("%10s",(actionTable[i].get(terminal) == null ? "|" : actionTable[i].get(terminal) + "|"));
            }
            str += "\n";
        }
        for (int j = 0; j < (terminals.size()+1)*10+2; j++) {
            str += "-";
        }
        return str;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public HashMap<String, Action>[] getActionTable() {
        return actionTable;
    }

    public HashMap<String, Integer>[] getGoToTable() {
        return goToTable;
    }
}
