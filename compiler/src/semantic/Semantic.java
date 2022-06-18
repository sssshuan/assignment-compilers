package semantic;

import parser.util.Grammar;
import parser.util.Rule;
import symbols.Array;
import symbols.Info;
import symbols.Type;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.logging.Logger;

public class Semantic {
    Hashtable<String, Info> top = new Hashtable<>(); // 记住标识符的附加信息

    private Stack<Symbol> symbols;

    private ArrayList<Code> codes;
    private ArrayList<Integer> arrs;

    private int tempNumber;
    private int cnt;

    private ArrayList<String> errors = new ArrayList<>();
    public ArrayList<String> getErrors() {
        return errors;
    }
    private ArrayList<String> threeAddressCode = new ArrayList<>();
    public ArrayList<String> getThreeAddressCode() {
        return threeAddressCode;
    }

    private Type type;
    private boolean ns = false;

    public Semantic() {      //无参构造函数
        this.symbols = new Stack<>();
        this.codes = new ArrayList<>();
        this.arrs= new ArrayList<>();
        tempNumber = 0;
        cnt = 0;
    }

    public String getTemp(){
        return "t"+(++tempNumber);
    }    //返回t1,t2之类的东西

    public void add(String first, String second){
        symbols.push(new Symbol(first, second, "null"));
//        System.out.println("呃呃呃呃呃"+  first + "\t" + second);
    }


    public void pop(){
        symbols.pop();
    }     //出栈一次


    //方便排查错误
    private void printSymbol() {
        for(Symbol symbol : symbols) {
            System.out.print("\t" + symbol.getFirst());
        }
        System.out.println();
    }
    /**
     *
     * @param rule 归约的规则
     */
    public void analyse(Rule rule){
        //归约后 弹出相应的右部符号 然后压入左部符号
        String left = rule.getLeftSide();

        printSymbol();
        System.out.println("规则:" + rule);

        switch (rule.toString()) {
            case "S -> program id { variable_declaration stmts } ": {
                symbols.pop();
                ArrayList<Integer> stmts_nextList = symbols.pop().getNextList();
                backpatch(stmts_nextList, nextInstr());
                symbols.pop();
                symbols.pop();
                symbols.pop();
                symbols.pop();
                break;
            }
            case "stmts -> stmts M stmt ": {
                ArrayList<Integer> stmt = symbols.pop().getNextList();
                int M = symbols.pop().getInstr();
                ArrayList<Integer> stmts1 = symbols.pop().getNextList();
                backpatch(stmts1, M);
                // stmts.nextList = stmt.nextList
                symbols.push(new Symbol(left, "null", "null", null, null, -1, stmt));
                break;
            }
            case "stmts -> stmt ": {
                ArrayList<Integer> stmt_nextList= symbols.pop().getNextList();
                symbols.push(new Symbol(left, "null", "null", null, null, -1, stmt_nextList));
                break;
            }
            case "stmt -> { stmts } ": {
                symbols.pop();
                ArrayList<Integer> stmts_nextList = symbols.pop().getNextList();
                symbols.pop();
                symbols.push(new Symbol(left, "null", "null", null, null, -1, stmts_nextList));
                break;
            }
            case "stmt -> while M ( expr ) M stmt ": {
                ArrayList<Integer> stmt1_nextList = symbols.pop().getNextList();
                int M2 = symbols.pop().getInstr();
                symbols.pop();
                ArrayList<Integer> trueList = symbols.peek().getTrueList();    //有碰到bool表达式才这样
                ArrayList<Integer> falseList = symbols.pop().getFalseList();
                symbols.pop();
                int M1 = symbols.pop().getInstr();
                symbols.pop();
                backpatch(stmt1_nextList,M1);
                backpatch(trueList,M2);
                symbols.push(new Symbol(left, "null", "null", null, null, -1, falseList));
                codes.add(new Code("goto", "null", "null", String.valueOf(M1 + 100)));
                break;
            }
            case "stmt -> if ( expr ) M stmt ": {
                ArrayList<Integer> stmt1_nextList = symbols.pop().getNextList();
                int M = symbols.pop().getInstr();     //M的作用是得到下一条跳转地址
                symbols.pop();
                ArrayList<Integer> trueList = symbols.peek().getTrueList();
                ArrayList<Integer> falseList = symbols.pop().getFalseList();
                symbols.pop();
                symbols.pop();
                backpatch(trueList,M);
                ArrayList<Integer> nextList = merge(falseList,stmt1_nextList);
                symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
                break;
            }
            case "stmt -> if ( expr ) M stmt N else M stmt ": {
                ArrayList<Integer> stmt2_nextList = symbols.pop().getNextList();
                int M2 = symbols.pop().getInstr();
                symbols.pop();
                ArrayList<Integer> N_nextList = symbols.pop().getNextList();     //N的处理与M稍微不同
                ArrayList<Integer> stmt1_nextList = symbols.pop().getNextList();
                int M1 = symbols.pop().getInstr();
                symbols.pop();
                ArrayList<Integer> trueList = symbols.peek().getTrueList();
                ArrayList<Integer> falseList = symbols.pop().getFalseList();
                symbols.pop();
                symbols.pop();
                backpatch(trueList,M1);
                backpatch(falseList,M2);
                ArrayList<Integer> nextList = merge(stmt1_nextList, N_nextList);
                nextList = merge(nextList, stmt2_nextList);
                symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
                break;
            }
            case "stmt -> for ( id in num .. num N ) M stmt ": {
                symbols.pop();
                int M_instr = symbols.pop().getInstr();
                symbols.pop();
                ArrayList<Integer> N_nextList = symbols.pop().getNextList();
                String num1_value = symbols.pop().getSecond();
                symbols.pop();
                String num_value = symbols.pop().getSecond();
                symbols.pop();
                String id_addr = symbols.pop().getSecond();
                symbols.pop();
                symbols.pop();

                String t = getTemp();
                codes.add(new Code("+", id_addr, "1", t));
                codes.add(new Code("=", t, "null", id_addr));
                int temp = nextInstr(); // 记住判断语句的位置
                codes.add(new Code("<=", id_addr, num1_value, String.valueOf(M_instr + 100)));

                ArrayList<Integer> nextList = new ArrayList<Integer>();
                nextList.add(nextInstr());
                symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
                codes.add(new Code("goto", "null", "null", "goto _"));
                backpatch(N_nextList, nextInstr());
                codes.add(new Code("=", ""+num_value, "null", id_addr)); // 初始值
                codes.add(new Code("goto", "null", "null", ""+(temp+100)));
                break;
            }
            case "stmt -> id = expr ; ": {
                symbols.pop();
                Type type_expr = symbols.peek().getType();//得到expr的类型
                String expr = symbols.pop().getAddr();   //表达式得到的是Addr
                symbols.pop();
                Symbol id = symbols.pop();
                String id_lexeme = id.getSecond();    //id用second
    Logger.getGlobal().severe(id_lexeme);
                Type id_type = top.get(id_lexeme).getType(); //得到id的类型
                symbols.push(new Symbol(left, "null", id_lexeme));
                expr = shorten(expr, type_expr, id_type); // 类型窄化
                codes.add(new Code("=", expr, "null",id_lexeme));
                top.get(id_lexeme).setNs(false);
                break;
            }
            case "stmt -> L = expr ; " : {
                symbols.pop();
                Symbol expr = symbols.pop();
                String expr_addr = expr.getAddr();
                symbols.pop();
                Symbol L = symbols.pop();
                String index_expr_addr = L.getAddr();
                String array = L.getSecond(); //取出具体的id

                if(!Type.numeric(L.getType()) && !L.getType().toString().equals(expr.getType().toString())) {
                    recordError("类型不匹配，预期: " + L.getType() + ", 实际: " + expr.getType());
                }

                symbols.push(new Symbol(left,"null",index_expr_addr));   //这里暂时不知道加什么
                expr_addr = shorten(expr_addr, expr.getType(), L.getType()); // 类型窄化
                codes.add(new Code("[]=",index_expr_addr,expr_addr,array));   //生成一条
                top.get(array).setNs(false);
                break;
            }
            case "stmt -> id -= expr ; ":
            case "stmt -> id += expr ; ":
            case "stmt -> id *= expr ; ":
            case "stmt -> id /= expr ; ": {
                symbols.pop();
                Symbol expr = symbols.pop();
                String expr_addr = expr.getAddr();
                String op = symbols.pop().getFirst();   //得到运算符
                String id = symbols.pop().getSecond();//getAddr();
                Type id_type = top.get(id).getType();
                if(top.get(id).isNs()) {
                    recordError("null value used '" + id + "'");
                    top.get(id).setNs(false); // 清掉，防止多次对同一标识符报错
                }
                symbols.push(new Symbol(left, "null", id));
                // t = id + expr
                Type max_type = Type.max(id_type, expr.getType());
                id = widen(id, id_type, max_type);
                expr_addr = widen(expr_addr, expr.getType(), max_type);
                String temp = getTemp();
                codes.add(new Code(""+op.charAt(0), id, expr_addr, temp));
                // id = t
                temp = shorten(temp, max_type, id_type);
                codes.add(new Code("=",temp,"null",id));
                break;
            }
            case "variable_declaration -> type null_sign variables ; variable_declaration ": {
                symbols.pop();
                symbols.pop();
                Symbol variables = symbols.pop();
                Symbol null_sign = symbols.pop();
                Symbol type = symbols.pop();
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "variable_declaration -> epsilon ": {
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "type -> int ": {
                symbols.pop();
                type = Type.Int;
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "type -> float ": {
                symbols.pop();
                type = Type.Float;
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "null_sign -> ? ": {
                symbols.pop();
                ns = true;
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "null_sign -> epsilon ": {
                ns = false;
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "variables -> variable , variables ":
            case "variables -> variable " : {
                for (int i = 0; i < rule.getRightSide().length; ++i) {
                    symbols.pop();
                }
                symbols.push(new Symbol(left, "null", "null"));
                break;
            }
            case "variable -> id array " :  {
                Symbol array = symbols.pop();
                Symbol id = symbols.pop();
                String id_lexeme = id.getSecond();
                if(top.get(id_lexeme) != null) {
                    recordError("重复声明: '" + id_lexeme + "'");
                }
                top.put(id_lexeme, new Info(array.getType(), ns));
//        Logger.getGlobal().info(id_lexeme + array.getType());
                symbols.push(new Symbol(left, "null", id_lexeme));
                break;
            }
            case "array -> [ num ] array " : {
                Symbol array_1 =  symbols.pop();
                symbols.pop();
                int num_value = Integer.parseInt(symbols.pop().getSecond());
//                Logger.getGlobal().info("测试数组下标: " + num_value);
                symbols.pop();
                Type arr = new Array(num_value, array_1.getType());
//                Logger.getGlobal().info("测试数组类型2： " + array_1.getType());
                symbols.push(new Symbol(left, "null", "null", arr));
                break;
            }
            case "array -> epsilon " : {
                symbols.push(new Symbol(left, "null", "null", type));
                break;
            }
            case "expr -> expr + expr ":
            case "expr -> expr - expr ":
            case "expr -> expr * expr ":
            case "expr -> expr / expr ":
            case "expr -> expr ^ factor ": {
                Symbol expr = new Symbol(left,"null","null");
                Type expr2_type = symbols.peek().getType();//取expr2的type
                String expr2_addr = symbols.pop().getAddr();
                String op = symbols.pop().getFirst();//取op
                Type expr1_type = symbols.peek().getType();//取expr1的type
                String expr1_addr = symbols.pop().getAddr();
                //获取两个操作数中级别最高的数据类型，赋给expr的type
                Type expr_type = Type.max(expr1_type,expr2_type);
                //按照最高级别类型做必要的操作数拓宽（强转）
                expr1_addr = widen(expr1_addr,expr1_type,expr_type);
                expr2_addr = widen(expr2_addr,expr2_type,expr_type);
                expr.setType(expr_type);
                String expr_addr = getTemp();
                expr.setAddr(expr_addr);
                symbols.push(expr);
                codes.add(new Code(op, expr1_addr, expr2_addr, expr_addr));    //加入三地址码
                break;
            }
            case "expr -> factor ": {
                Symbol factor = symbols.pop();
                String tmp = factor.getAddr();
                symbols.push(new Symbol(left, "null", tmp, factor.getType()));  //addr放到addr
                break;
            }
            case "expr -> expr < expr ":
            case "expr -> expr > expr ":
            case "expr -> expr != expr ":
            case "expr -> expr == expr ":
            case "expr -> expr <= expr ":
            case "expr -> expr >= expr ": {
                String expr2 = symbols.pop().getAddr();
                String op = symbols.pop().getFirst();     //运算符getfirst，id什么的getsecond
                String expr1 = symbols.pop().getAddr();
                ArrayList<Integer> trueList = new ArrayList<Integer>();
                trueList.add(codes.size());
                ArrayList<Integer> falseList = new ArrayList<Integer>();
                falseList.add(codes.size()+1);
                symbols.push(new Symbol(left, "null", "null", trueList, falseList));
                codes.add(new Code(op, expr1, expr2, "goto _"));
                codes.add(new Code("goto", "null", "null", "goto _"));
                break;
            }
            case "expr -> expr && M expr " : {
                ArrayList<Integer> trueList2 = symbols.peek().getTrueList();
                ArrayList<Integer> falseList2 = symbols.pop().getFalseList();
                int M = symbols.pop().getInstr();
                symbols.pop();
                ArrayList<Integer> trueList1 = symbols.peek().getTrueList();
                ArrayList<Integer> falseList1 = symbols.pop().getFalseList();
                backpatch(trueList1,M);
                ArrayList<Integer> falseList = new ArrayList<Integer>();
                falseList = merge(falseList1,falseList2);
                symbols.push(new Symbol(left, "null", "null", trueList2, falseList, -1));
                break;
            }
            case "expr -> expr || M expr " : {
                ArrayList<Integer> trueList2 = symbols.peek().getTrueList();
                ArrayList<Integer> falseList2 = symbols.pop().getFalseList();
                int M = symbols.pop().getInstr();
                symbols.pop();
                ArrayList<Integer> trueList1 = symbols.peek().getTrueList();
                ArrayList<Integer> falseList1 = symbols.pop().getFalseList();
                backpatch(falseList1,M);
                ArrayList<Integer> trueList = new ArrayList<Integer>();
                trueList = merge(trueList1,trueList2);
                symbols.push(new Symbol(left, "null", "null", trueList, falseList2, -1));
                break;
            }
            case "factor -> L " : {
                String temp = getTemp();   //新生成temp
                Symbol L = symbols.pop();
                String L_addr = L.getAddr();
                String id = L.getSecond();
                symbols.push(new Symbol(left,id,temp, L.getType()));   //这个也是随便弄的
                codes.add(new Code("=[]",id,L_addr,temp));
                break;
            }
            case "factor -> id " : {
                String id = symbols.pop().getSecond();
//                if(top.get(id) == null) {
//                    recordError("使用未声明变量 '" + id + "'");
//                    break;
//                }
//                else
                if(top.get(id).isNs()) {
                    recordError("null value used '" + id + "'");
                    top.get(id).setNs(false); // 清掉，防止多次对同一标识符报错
                }
                symbols.push(new Symbol(left, "null", id, top.get(id).getType()));   //second放到addr
                break;
            }
            case "factor -> num " : {
                String tmp = symbols.pop().getSecond();
                symbols.push(new Symbol(left, "null", tmp, Type.Int));   //second放到addr
                break;
            }
            case "factor -> real " : {
                String tmp = symbols.pop().getSecond();
                symbols.push(new Symbol(left, "null", tmp, Type.Float));   //second放到addr
                break;
            }
            case "factor -> ( expr ) " : {
                symbols.pop();
                String expr = symbols.pop().getAddr();
                symbols.pop();
                symbols.push(new Symbol(left, "null", expr));  //addr直接继承
                break;
            }
            case "M -> epsilon " : {
                symbols.push(new Symbol(left, "null", "null", null, null, nextInstr()));
                break;
            }
            case "N -> epsilon " : {
                ArrayList<Integer> nextList = new ArrayList<Integer>();
                nextList.add(nextInstr());
                symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
                codes.add(new Code("goto", "null", "null", "goto _"));
                break;
            }
            case "L -> id [ expr ] ": {
                String L = getTemp();
                symbols.pop();
                Symbol expr = symbols.pop();
                String expr_addr = expr.getAddr();
                symbols.pop();
                Symbol id = symbols.pop();
                String id_lexeme = id.getSecond();
                Array id_type = (Array) top.get(id_lexeme).getType();//new Array(10, new Array(20, Type.Int));
                symbols.push(new Symbol(left, id_lexeme, L, id_type.element)); // 子数组类型
                expr_addr = shorten(expr_addr, expr.getType(), Type.Int);
                codes.add(new Code("*",expr_addr, ""+id_type.element.width, L));
                break;
            }
            case "L -> L [ expr ] ": {
                String t = getTemp();
                String L_addr = getTemp();
                symbols.pop();
                Symbol expr = symbols.pop();
                String expr_addr = expr.getAddr();
                symbols.pop();
                Symbol L1 = symbols.pop();
                Array L1_array = (Array)L1.getType();
                String L1_addr = L1.getAddr();
                String id = L1.getSecond();
                symbols.push(new Symbol(left, id, L_addr, L1_array.element)); //子数组类型
                expr_addr = shorten(expr_addr, expr.getType(), Type.Int);
                codes.add(new Code("*", expr_addr, "" + L1_array.element.width, t));
                codes.add(new Code("+", L1_addr, t , L_addr));
                break;
            }
            default: {
                throw new Error("没有匹配规则");
//                Logger.getGlobal().severe("没有匹配规则");
//                for(int i = 0;i < rule.getRightSide().length;i++){
//                    symbols.pop();
//                }
//                symbols.push(new Symbol(left, "null", "null"));
            }
        }
    }

    public void backpatch(ArrayList<Integer> list, int m){  //回填函数没问题
        if(list!=null&&list.size()!=0) {
            for (int i = 0; i < list.size(); i++) {
                int j = list.get(i);
                //System.out.println(1);
                codes.get(j).setResult(String.valueOf(m + 100)); //填入指令标号
            }
        }
    }

    public ArrayList<Integer> merge(ArrayList<Integer> list1, ArrayList<Integer>list2){   //这个是merge函数
        ArrayList<Integer> list=new ArrayList<Integer>();
        if(list1==null||list1.size()==0){
            list=list2;
        }else if(list2==null||list2.size()==0){
            list=list1;
        }else {
            for (int i = 0; i < list1.size();i++){
                list.add(list1.get(i));
            }
            for(int j = 0; j < list2.size();j++ ){
                list.add(list2.get(j));
            }
        }
        return list;
    }



    public String printList(int list){
        return list == -1 ? "null":list+"";
    }  //返回list

    public void print(){    //这个是打印三地址码的函数
        for(int i = 0;i < codes.size();i++){
            StringBuilder s = new StringBuilder();
            Code x = codes.get(i);
            int addr = i+100;
            //如果是运算符的话
            if(x.getOp().equals("+") || x.getOp().equals("-") || x.getOp().equals("*") || x.getOp().equals("/") || x.getOp().equals("^")){
                if(x.getArg2().equals("null")){
                    int num = arrs.get(cnt++); //干嘛的
                    x.setArg2(String.valueOf(num));
                    Logger.getGlobal().severe("啥");
                }
                s.append(x.getResult()).
                        append(" = ").
                        append(x.getArg1()).
                        append(" ").
                        append(x.getOp()).
                        append(" ").append(x.getArg2());
            } else if(x.getOp().equals("=")){
                //如过是赋值这种
                s.append(x.getResult()).append(" = ").append(x.getArg1());
                //如果是bool表达式的话，处理goto语句
            }
            else if(x.getOp().equals("+=") || x.getOp().equals("-=") || x.getOp().equals("/=") || x.getOp().equals("*=")){
                String h = x.getOp();
                String op1 = String.valueOf(h.charAt(0));
                s.append(x.getResult()).
                        append(" = ").
                        append(x.getArg1()).
                        append(op1).
                        append(x.getArg2());
            }
            else if(x.getOp().equals("[]=")){
                s.append(x.getResult()).
                        append("[").
                        append(x.getArg1()).
                        append("]").
                        append(" = ").
                        append(x.getArg2());
            }
            else if(x.getOp().equals("=[]")){
                s.append(x.getResult()).
                        append(" = ").
                        append(x.getArg1()).
                        append("[").
                        append(x.getArg2()).
                        append("]");
            }
            else if(x.getOp().equals("<") || x.getOp().equals(">") || x.getOp().equals("<=") || x.getOp().equals(">=")||x.getOp().equals("==")||x.getOp().equals("!=")){
                s.append("if ").
                        append(x.getArg1()).
                        append(" ").
                        append(x.getOp()).
                        append(" ").
                        append(x.getArg2()).
                        append(" goto ").append(x.getResult());
            }else if(x.getOp().equals("goto")){
                s.append("goto ").append(x.getResult());
            }else
                continue;
            threeAddressCode.add(addr+": " + String.valueOf(s));
            System.out.println(addr+": " + String.valueOf(s));
        }
        System.out.println((codes.size()+100)+": "+ " ");
        threeAddressCode.add((codes.size()+100)+": "+ " ");
    }

    public int size() {
        return symbols.size();
    }    //返回大小


    //conds.size刚好是下一条指令的地址
    private int nextInstr() {
        return codes.size();
    }

    /**
     * 拓宽类型
     */
    private String widen(String addr,Type t,Type w){
        if(t.toString().equals(w.toString())) return addr;
        else{//类型不同，需要强转
            String temp= getTemp();
            codes.add(new Code("=","("+w.lexeme+")"+addr,"null",temp));
            return temp;
        }

    }

    /**
     * 窄化类型
     */
    private String shorten(String addr, Type operator, Type result) {
        if(operator.toString().equals(result.toString()))
            return addr;
        else {
            String t = getTemp();
            codes.add(new Code("=","("+result.lexeme+")"+addr,"null",t));
            return t;
        }
    }


    private void recordError(String msg) {
        String error = "Error: " + msg;
        if (errors.contains(error)) {
            return;
        }
        errors.add(error);
    }

}
