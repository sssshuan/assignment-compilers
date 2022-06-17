package parser.util;

import lexer.Token;
import semantic.Semantic;

import javax.swing.table.DefaultTableModel;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public abstract class LRParser {
    private Semantic semantic;

    //在数组中的索引 与 状态 对应
    //键为非终结符，值为要移入的状态
    protected HashMap<String, Integer>[] goToTable;
    //在数组中的索引 与 状态 对应
    //键为终结符，值为操作
    protected HashMap<String, Action>[] actionTable;
    protected Grammar grammar;

    private Stack<String> tokenStack = new Stack<>(); //符号栈
    private Stack<Integer> stateStack = new Stack<>(); //状态栈

    //记录每次操作，用于显示分析过程
    private List<List<String>> result = new ArrayList<>();
    FileOutputStream outputStream; //输出错误

    public LRParser(Grammar grammar) {
        this.grammar = grammar;
        semantic = new Semantic(grammar, new DefaultTableModel(), new DefaultTableModel());
    }

    protected abstract void createGoToTable();

    public List<List<String>> getResult() {
        return result;
    }

    /**
     * 分析过程
     */
    public boolean accept(ArrayList<Token> inputs) {
        try {
            outputStream = new FileOutputStream("error.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        result = new ArrayList<>();

        inputs.add(new Token('$'));
        int inputIndex = 0;
        tokenStack = new Stack<>(); //符号栈
        stateStack = new Stack<>(); //状态栈

        stateStack.push(0);
        tokenStack.push("$");

        while(inputIndex < inputs.size()){
            int state = stateStack.peek();
            Token nextInputSymbol = inputs.get(inputIndex); //下一个输入
            String nextInput = nextInputSymbol.first(); //取输入的字符串(主要是 id num比较特殊，语法分析用id，而id的词法值在second，用于语义)
            Action action = actionTable[state].get(nextInput);
            if(action == null){
                // 表项为空
                Logger.getGlobal().severe("表项<" + state + ","+ nextInput + ">为空");

                String errorMsg = "error: 状态 \"" + state + "\", 符号栈顶: \"" + tokenStack.peek() + "\", 输入: \"" + nextInput + "\", 分析表中没有对应规则, 丢弃该输入\n";
                saveErrorToFile(errorMsg);
                inputIndex++; //跳过该输入
                recordAction(new Action(ActionType.SKIP, 0));
            }else if(action.getType() == ActionType.SHIFT){
                //移入
                tokenStack.push(nextInput);
                stateStack.push(action.getOperand());
                inputIndex++;
                recordAction(action);
//Logger.getGlobal().info(nextInput+"\t"+nextInputSymbol.second());
                //移入时，把符号的词法值给计算了（主要是id.lexeme，num.value），后续归约时可以用到
                semantic.add(nextInput, nextInputSymbol.second());
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
                    tokenStack.pop();
                    stateStack.pop();
                }

                int nextState = stateStack.peek();
                tokenStack.push(leftSide);
                int variableState = goToTable[nextState].get(leftSide);
                stateStack.push(variableState);
                recordAction(action);
                //归约时，对语义的栈做相应操作
                semantic.analyse(rule);
            }else if(action.getType() == ActionType.ACCEPT){
                //接受
                recordAction(action);
                //输出中间代码
                semantic.print();
                return true;
            }
        }
        //失败
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

    /**
     * 记录操作
     */
    private void recordAction(Action action) {
        List<String> record = new ArrayList<>(); //状态栈 符号栈 动作

        if(action.getType() == ActionType.SKIP) {
            record.add("");
            record.add("");
            record.add("跳过输入符号");
        }
        else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < stateStack.size(); ++i) {
                builder.append(stateStack.get(i) + " ");
            }
            record.add(builder.toString());

            StringBuilder stackBuilder = new StringBuilder();
            for (int i = 0; i < tokenStack.size(); ++i) {
                stackBuilder.append(tokenStack.get(i) + " ");
            }
            record.add(stackBuilder.toString()); // 符号栈

            // 动作
            if (action.getType() == ActionType.REDUCE) {
                int ruleIndex = action.getOperand();
                Rule rule = grammar.getRules().get(ruleIndex);
                record.add("Reduce " + rule);
            } else {
                record.add(action.toString());
            }
        }

        result.add(record);
    }

    private void saveErrorToFile(String msg) {
        try {
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
