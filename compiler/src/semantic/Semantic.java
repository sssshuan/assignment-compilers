package semantic;

import parser.util.Grammar;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Stack;

public class Semantic {
    private Stack<Symbol> symbols;
    private ArrayList<Code> codes;
    private ArrayList<Integer> arrs;
//    private AnalyseList analyseList;
    private Grammar grammar;

    private int tempNumber;
    private int cnt;
    private DefaultTableModel tbmodel_expanded_stack;
    private DefaultTableModel tbmodel_addr_code;
    public int arraysizea = 1;
    public int arraysizeb = 2;
    public int arraysizec = 3;
    public String py;
    public Semantic() {      //无参构造函数
        this.symbols = new Stack<>();
        this.codes = new ArrayList<>();
        this.arrs= new ArrayList<>();
        tempNumber = 0;
        cnt = 0;
    }

    public Semantic(Grammar grammar, DefaultTableModel tbmodel_expanded_stack,
                    DefaultTableModel tbmodel_addr_code){      //有参构造函数，传入两个表
        this();
        this.grammar = grammar;
        this.tbmodel_addr_code = tbmodel_addr_code;
        this.tbmodel_expanded_stack = tbmodel_expanded_stack;
    }

    public String getTemp(){
        return "t"+(++tempNumber);
    }    //返回t1,t2之类的东西

    public void add(String first, String second){
        symbols.push(new Symbol(first, second, "null"));
//        Symbol t = symbols.peek();
//        tbmodel_expanded_stack.addRow(new String[]{t.getFirst(), t.getSecond(), t.getAddr(),
//                printList(t.getTrueList()), printList(t.getFalseList()), printList(t.getNextList())});
    }


    public void pop(){
        symbols.pop();
    }     //出栈一次


    /**
     *
     * @param res res为第几个产生式
     * @param l 规则右部几个词法单元？
     */
    public void analyse(int res, int l){
        //归约后 弹出相应的右部符号 然后压入左部符号
        String left = grammar.getRules().get(res).getLeftSide();//analyseList.productions.get(res).returnLeft();   //返回文法的左部
//        ArrayList<Symbol> out = new ArrayList<>();
        if(res == 30 || res == 31){
            // factor -> id | number
            String tmp = symbols.peek().getSecond();
            for(int i = 0;i < l;i++) // 这边不是要么0要么1吗？ epsilon规则对应l为0
                symbols.pop();
            symbols.push(new Symbol(left, "null", tmp));   //second放到addr
        }
        else if(res == 2){
            //stmts -> { stmt M }
            symbols.pop();
            int M =symbols.pop().getInstr();
            ArrayList<Integer> stmts = symbols.pop().getNextList();
            symbols.pop();
            backpatch(stmts,M);
            symbols.push(new Symbol(left, "null", "null"));
        }
        //stmt -> stmts
        else if(res == 3){
            ArrayList<Integer> stmts= symbols.pop().getNextList();
            symbols.push(new Symbol(left, "null", "null", null, null, -1, stmts));
        }
        //stmt -> stmt M stmts
        else if(res == 4){
            ArrayList<Integer> stmts= symbols.pop().getNextList();
            int M = symbols.pop().getInstr();
            ArrayList<Integer> stmt= symbols.pop().getNextList();
            backpatch(stmt,M);
            symbols.push(new Symbol(left, "null", "null", null, null, -1, stmts));
        }
        else if(res == 29){
            // expr -> factor
            String tmp = symbols.peek().getAddr();       //addr
            for(int i = 0;i < l;i++)
                symbols.pop();
            symbols.push(new Symbol(left, "null", tmp));  //addr放到addr
        } else if(res >= 24 && res <= 28){
            // 四则运算 E = E op E
            // 此时栈顶是 E1 op E2，弹出后生成代码并把临时变量压栈
            String factor = symbols.pop().getAddr();
            String op = symbols.pop().getFirst();
            String term1 = symbols.pop().getAddr();
            String term = getTemp();
            symbols.push(new Symbol(left, "null", term));
            codes.add(new Code(op, term1, factor, term));    //加入三地址码
        }
        else if(res >= 8 && res <= 11){
        //stmts -> id += expr ；
            symbols.pop();
            String expr = symbols.pop().getAddr();
            String op = symbols.pop().getFirst();   //得到运算符
            String id = symbols.pop().getSecond();   //得到id
            symbols.push(new Symbol(left, "null", id));
            codes.add(new Code(op,id,expr,id));
        }
       // bool -> bool && M bool
        else if(res == 15 ){
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
        }
        else if(res == 16 ){
            // bool -> bool || M bool
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
        }
        else if(res == 5 || res == 6){
            // stmts -> id = expr ;
            symbols.pop();
            String expr = symbols.pop().getAddr();   //表达式得到的是Addr
            symbols.pop();
            String id = symbols.pop().getSecond();    //id用second
            symbols.push(new Symbol(left, "null", id));
            codes.add(new Code("=", expr, "null", id));
        }
        else if( res == 7){
            //stmts -> L = expr ; gen(L.array.base '['L.addr']' ' =' E.addr) op []=  arg1t1    arg2t2
            symbols.pop();
            String expr = symbols.pop().getAddr();
            symbols.pop();
            String id = symbols.peek().getAddr();     //取出具体的id
            String array = symbols.pop().getSecond();
            symbols.push(new Symbol(left,"null",id));   //这里暂时不知道加什么
            codes.add(new Code("[]=",id,expr,array));   //生成一条
            arraysizea = 1;
            arraysizeb = 2;
            arraysizec = 3;
        }
        //数组可以为a[10]    a[10][20]    a[10][20][30]
        else if(res == 35) {
            //expr -> L       E.addr = new Temp();  gen(E.addr ' =' L.array.base '[' L.addr ']');
            String term = getTemp();   //新生成temp
            String L = symbols.peek().getAddr();
            String id = symbols.pop().getSecond();
            symbols.push(new Symbol(left,id,term));   //这个也是随便弄的
            codes.add(new Code("=[]",id,L,term));
            
        }
        else if(res == 36){  
            //L -> id [ expr ]     id E.addr    L.addr = new temp();   gen(L.addr '=' E.addr '*'L.wideth)
            String L = getTemp();
            symbols.pop();
            String expr = symbols.pop().getAddr();
            symbols.pop();
            String id = symbols.pop().getSecond();
            symbols.push(new Symbol(left,id,L));
//            System.out.println(id);
            if (arraysizea==1&&id.equals("a")) { 
            	py = "4";
            	arraysizea--;
            }
            if (id.equals("b")) { 
            	py = "80";
            	arraysizeb--;
            }
            if (id.equals("c")) { 
            	py = "2400";
            	arraysizec--;
            }
            codes.add(new Code("*",expr,py,L));  
        }else if(res == 37){
            //L -> L [ expr ]
            String t= getTemp();
            String L =getTemp();
            symbols.pop();
            String expr = symbols.pop().getAddr();
            symbols.pop();
            String L1 = symbols.peek().getAddr();
            String id = symbols.pop().getSecond();
            symbols.push(new Symbol(left,id,L));          
            if (arraysizeb==1&&id.equals("b")) { 
            	py = "4";
            	arraysizeb--;
            }
            if (arraysizec==1&&id.equals("c")) { 
            	py = "4";
            	arraysizec--;
            }
            if (arraysizec==2&&id.equals("c")) { 
            	py = "80";
            	arraysizec--;
            }
            System.out.println(py);
            codes.add(new Code("*",expr,py,t)); 
            codes.add(new Code("+",L1,t,L));
        }
        else if(res >=18 && res <= 23){
            // bool -> expr rel expr
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
        } else if(res == 33){
            // M -> epsilon
            symbols.push(new Symbol(left, "null", "null", null, null, codes.size()));   //conds.size刚好是下一条指令的地址
        } else if(res == 34){
            // N -> epsilon
            ArrayList<Integer> nextList = new ArrayList<Integer>();
            nextList.add(codes.size());
            symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
            codes.add(new Code("goto", "null", "null", "goto _"));
        } else if(res == 13){
            // stmts -> if ( bool ) M stmts
            ArrayList<Integer> stmt1 = symbols.pop().getNextList();
            int M = symbols.pop().getInstr();     //M的作用是得到下一条跳转地址
            symbols.pop();
            ArrayList<Integer> trueList = symbols.peek().getTrueList();
            ArrayList<Integer> falseList = symbols.pop().getFalseList();
            symbols.pop();   //if直接pop
            symbols.pop();
            backpatch(trueList,M);
            ArrayList<Integer> nextList =  new ArrayList<Integer>();
            nextList = merge(falseList,stmt1);
            symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
        }
        else if(res == 12){
            // stmts -> if ( bool )  M stmts N else M stmts
            ArrayList<Integer> stmt2 = symbols.pop().getNextList();
            int M2 = symbols.pop().getInstr();
            symbols.pop();
            ArrayList<Integer> N = symbols.pop().getNextList();     //N的处理与M稍微不同
            ArrayList<Integer> stmt1 = symbols.pop().getNextList();
            int M1 = symbols.pop().getInstr();
            symbols.pop();
            ArrayList<Integer> trueList = symbols.peek().getTrueList();
            ArrayList<Integer> falseList = symbols.pop().getFalseList();
            symbols.pop();
            symbols.pop();
            backpatch(trueList,M1);
            backpatch(falseList,M2);
            ArrayList<Integer> nextList = new ArrayList<Integer>();   //这里有一个多余的报错
            nextList = merge(stmt1,N);
            nextList = merge(nextList,stmt2);
            symbols.push(new Symbol(left, "null", "null", null, null, -1, nextList));
        }
        //S -> program id { stmt }
//        else if (res == 1) {
//            symbols.pop();   //end进行pop
//            int M = symbols.pop().getInstr();
//            int stmts = symbols.pop().getNextList();
//            symbols.pop();  //begin进行pop
//            if(stmts != -1)
//                codes.get(stmts).setResult(String.valueOf(M + 100));
//            symbols.push(new Symbol(left, "null", "null"));
//        }
            else if(res == 14){
            // while_stmt -> while M ( bool ) M stmts
            ArrayList<Integer> stmt = symbols.pop().getNextList();
            int M2 = symbols.pop().getInstr();
            symbols.pop();
            ArrayList<Integer> trueList = symbols.peek().getTrueList();    //有碰到bool表达式才这样
            ArrayList<Integer> falseList = symbols.pop().getFalseList();
            symbols.pop();
            int M1 = symbols.pop().getInstr();
            symbols.pop();
            backpatch(stmt,M1);
            backpatch(trueList,M2);
            symbols.push(new Symbol(left, "null", "null", null, null, -1, falseList));
            codes.add(new Code("goto", "null", "null", String.valueOf(M1 + 100)));
        }
        else if(res == 32){
            // factor -> ( expr )
            symbols.pop();
            String expr = symbols.pop().getAddr();
            symbols.pop();
            symbols.push(new Symbol(left, "null", expr));  //addr直接继承
        } else {
            for(int i = 0;i < l;i++){
                symbols.pop();
            }
            symbols.push(new Symbol(left, "null", "null"));
        }
        Symbol t = symbols.peek();
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
                if(x.getArg2()=="null"){
                    int num = arrs.get(cnt++);
                    x.setArg2(String.valueOf(num));
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
            //System.out.println(String.valueOf(addr)+": "+s);
System.out.println(addr+": " + String.valueOf(s));
            tbmodel_addr_code.addRow(new String[]{addr+": ", String.valueOf(s)});    //最后实际在这里输出
        }
System.out.println((codes.size()+100)+": "+ " ");
        tbmodel_addr_code.addRow(new String[]{(codes.size()+100)+": ", " "});     //这个是输出大小的
        //System.out.println(codes.size()+100+": ");
    }

    public int size() {
        return symbols.size();
    }    //返回大小
}
