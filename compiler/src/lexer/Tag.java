package lexer;

public class Tag {
    /**
     * 定义了各个词法单元对应的常量
     * 其中的三个常量INDEX、MINUS、和TEMP不是词法单元，它们将在抽象语法树当中使用。
     */
    public final static int
            AND = 256, BASIC = 257, BREAK = 258, DO = 259, ELSE = 260,
            EQ = 261, FALSE = 262, GE = 263, ID = 264, IF = 265,
            INDEX = 266, LE = 267, MINUS = 268, NE = 269, NUM = 270,
            OR = 271, REAL = 272, TEMP = 273, TRUE = 274, WHILE = 275,
            CONTINUE = 276;
}
