package parser.lr0;

import parser.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class LR0Parser extends LRParser {
    
    private ArrayList<LR0State> canonicalCollection; //全部状态
    
    public LR0Parser(Grammar grammar) {
        super(grammar);
    }
    
    public boolean parserSLR1() {
        createStates();
        createGoToTable();
        return createActionTableForSLR1();
    }

//    public boolean parserLR0() {
//        createStates();
//        createGoToTable();
//        return createActionTableForLR0();
//    }
    
    protected void createStates() {
        canonicalCollection = new ArrayList<>();
        HashSet<LR0Item> start = new HashSet<>();
        start.add(new LR0Item(grammar.getRules().get(0)));
        
        LR0State startState = new LR0State(grammar, start);
        canonicalCollection.add(startState);
        
        for (int i = 0; i < canonicalCollection.size(); i++) {
            HashSet<String> stringWithDot = new HashSet<>(); //后继符号（除了归约项）
            for (LR0Item item : canonicalCollection.get(i).getItems()) {
                if (item.getCurrentTerminal() != null) { //"点"还没到最右端的项
                    stringWithDot.add(item.getCurrentTerminal());
                }
            }
            for (String str : stringWithDot) {
                HashSet<LR0Item> nextStateItems = new HashSet<>(); // 下一状态的项集
                for (LR0Item item : canonicalCollection.get(i).getItems()) {
                    if (item.getCurrentTerminal() != null && item.getCurrentTerminal().equals(str)) {
                        LR0Item temp = new LR0Item(item);
                        temp.goTo(); // 该项的"点"右移一位，加入下一状态
                        nextStateItems.add(temp);
                    }
                }
                LR0State nextState = new LR0State(grammar, nextStateItems);
                boolean isExist = false;
                for (int j = 0; j < canonicalCollection.size(); j++) {
                    if (canonicalCollection.get(j).getItems().containsAll(nextState.getItems())
                            && nextState.getItems().containsAll(canonicalCollection.get(j).getItems())) {
                        isExist = true;
                        canonicalCollection.get(i).addTransition(str, canonicalCollection.get(j));
                    }
                }
                if (!isExist) {
                    canonicalCollection.add(nextState);
                    canonicalCollection.get(i).addTransition(str, nextState);
                }
            }
        }
        
    }
    
    protected void createGoToTable() {
        goToTable = new HashMap[canonicalCollection.size()];
        for (int i = 0; i < goToTable.length; i++) {
            goToTable[i] = new HashMap<>();
        }
        for (int i = 0; i < canonicalCollection.size(); i++) {
            for (String s : canonicalCollection.get(i).getTransition().keySet()) {
                if (grammar.getVariables().contains(s)) {
                    goToTable[i].put(s, findStateIndex(canonicalCollection.get(i).getTransition().get(s)));
                }
            }
        }
    }
    
    private boolean createActionTableForSLR1() {
        actionTable = new HashMap[canonicalCollection.size()];
        for (int i = 0; i < goToTable.length; i++) {
            actionTable[i] = new HashMap<>();
        }
        for (int i = 0; i < canonicalCollection.size(); i++) {
            for (String s : canonicalCollection.get(i).getTransition().keySet()) {
                if (grammar.getTerminals().contains(s)) {

                    actionTable[i].put(s, new Action(ActionType.SHIFT, findStateIndex(canonicalCollection.get(i).getTransition().get(s))));
                }
            }
        }
        for (int i = 0; i < canonicalCollection.size(); i++) {
            for (LR0Item item : canonicalCollection.get(i).getItems()) {
                if (item.getDotPointer() == item.getRightSide().length) { //归约
                    if (item.getLeftSide().equals("S'")) {
                        actionTable[i].put("$", new Action(ActionType.ACCEPT, 0));
                    } else {
                        HashSet<String> follow = grammar.getFallowSets().get(item.getLeftSide());
                        Rule rule = new Rule(item.getLeftSide(), item.getRightSide().clone());
                        int index = grammar.findRuleIndex(rule);
                        Action action = new Action(ActionType.REDUCE, index); // 归约
                        for (String str : follow) {
                            if (actionTable[i].get(str) != null) {
                                //提示错误
                                System.out.println("\"" + action + "\" conflict with \"" + actionTable[i].get(str) + "\" in state " + i);
                                if(actionTable[i].get(str).getType() == ActionType.REDUCE) {
                                    //归约/归约冲突 错误
                                    return false;
                                }else {
                                    // 简单找出该归约规则的操作符 （先默认表达式只有一个操作符。。。）
                                    String newOp = "";
                                    for (String op : rule.getRightSide()) {
                                        if (ambiguousOperator(op)) {
                                            newOp = op;
                                            break;
                                        }
                                    }
                                    // 特判 保留一个操作
                                    Action reservedAction = solveConflict(actionTable[i].get(str), str, action, newOp);
                                    actionTable[i].put(str,reservedAction);
                                    System.out.println("\tfix conflict: keep action \""+reservedAction+"\"");
                                }
                            } else {
                                actionTable[i].put(str, action);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

//    private boolean createActionTableForLR0() {
//        actionTable = new HashMap[canonicalCollection.size()];
//        for (int i = 0; i < goToTable.length; i++) {
//            actionTable[i] = new HashMap<>();
//        }
//        for (int i = 0; i < canonicalCollection.size(); i++) {
//            for (String s : canonicalCollection.get(i).getTransition().keySet()) {
//                if (grammar.getTerminals().contains(s)) {
//                    actionTable[i].put(s, new Action(ActionType.SHIFT, findStateIndex(canonicalCollection.get(i).getTransition().get(s))));
//                }
//            }
//        }
//        for (int i = 0; i < canonicalCollection.size(); i++) {
//            for (LR0Item item : canonicalCollection.get(i).getItems()) {
//                if (item.getDotPointer() == item.getRightSide().length) {
//                    if (item.getLeftSide().equals("S'")) {
//                        actionTable[i].put("$", new Action(ActionType.ACCEPT, 0));
//                    } else {
//                        HashSet<String> terminals = grammar.getTerminals();
//                        terminals.add("$");
//                        Rule rule = new Rule(item.getLeftSide(), item.getRightSide().clone());
//                        int index = grammar.findRuleIndex(rule);
//                        Action action = new Action(ActionType.REDUCE, index);
//                        for (String str : terminals) {
//                            if (actionTable[i].get(str) != null) {
//                                System.out.println("it has a REDUCE-" + actionTable[i].get(str).getType() + " conflict in state " + i);
//                                return false;
//                            } else {
//                                actionTable[i].put(str, action);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }
    
    private int findStateIndex(LR0State state) {
        for (int i = 0; i < canonicalCollection.size(); i++) {
            if (canonicalCollection.get(i).equals(state)) {
                return i;
            }
        }
        return -1;
    }

//    public String canonicalCollectionStr() {
//        String str = "Canonical Collection : \n";
//        for (int i = 0; i < canonicalCollection.size(); i++) {
//            str += "State " + i + " : \n";
//            str += canonicalCollection.get(i)+"\n";
//        }
//        return str;
//    }

    /**
     * 特判解决冲突（相当于手动调整）
     * @param shiftAction 移入操作
     * @param shiftOp 移入操作对应的运算符
     * @param reduceAction 归约操作
     * @param reduceOp 归约操作对应的运算符
     * @return 返回要保留的操作
     */
    private Action solveConflict(Action shiftAction, String shiftOp, Action reduceAction, String reduceOp) {
        // * 优先于 + ，所以优先移入
        if(reduceOp.equals("+") && shiftOp.equals("*")) {
            return shiftAction;
        }

        // 剩下的情况，归约优先
        return reduceAction;
    }

    /**
     * 判断运算符是否属于产生冲突的符号集
     * @param op 要判断的符号
     * @return true表示属于
     */
    private boolean ambiguousOperator(String op) {
        //出现二义冲突的符号，手动添加到这里
        String[] arr = new String[]{"*", "+"};
        for(String str : arr) {
            if(str.equals(op)) {
                return true;
            }
        }
        return false;
    }

}
