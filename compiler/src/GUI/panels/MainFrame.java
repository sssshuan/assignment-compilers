package GUI.panels;

import lexer.*;
import parser.lr0.LR0Parser;
import parser.util.Grammar;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * @program: assignment-compilers
 * @description: 主界面
 * @author: ysj
 * @date: 2022-05-13 15:56
 **/
public class MainFrame extends JFrame {
    private JLabel inputLabel;
    private JTextArea inputArea;
    private Button inputBtn;

    private JLabel outputLabel;
    private JTextArea outputArea;
    private Button outputBtn;

    private Button analyseBtn;//开始转换按钮
    private JFileChooser chooser;//文件选择器
    private File file;
    private String content = "";//读取的内容
    private String result = "";//分析结果

    LR0Parser lr0Parser;

    public MainFrame() throws HeadlessException {
        super("词法分析器");
        chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("标签文件(*.txt)", "txt");
        chooser.setFileFilter(filter);
        initFrame();
        initEvents();
        pack();
        // 根据文法 初始化语法分析器
        initParser();
    }

    private void initFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setLocation(0, 0);

        //输入盒子
        inputLabel = new JLabel("源代码:");
        inputArea = new JTextArea(25, 40);
        inputArea.setFont(new Font("monospaced", Font.PLAIN, 15));
        JScrollPane jScrollPane1 = new JScrollPane(inputArea);
        inputBtn = new Button("读取文件");
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(jScrollPane1, BorderLayout.CENTER);
        inputPanel.add(inputBtn, BorderLayout.SOUTH);

        //输出盒子
        outputLabel = new JLabel("结果:");
        outputArea = new JTextArea(25, 40);
        outputArea.setFont(new Font("monospaced", Font.PLAIN, 15));
        outputArea.setLineWrap(true);
        JScrollPane jScrollPane2 = new JScrollPane(outputArea);
        outputBtn = new Button("保存结果");
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(jScrollPane2, BorderLayout.CENTER);
        outputPanel.add(outputBtn, BorderLayout.SOUTH);
        analyseBtn = new Button("开始分析>>>");

        initTableUI();

        //界面添加
        setLayout(new FlowLayout());
        add(inputPanel);
        add(analyseBtn);
        add(outputPanel);
        add(new JScrollPane(scoreTable));
    }

    class SHTable extends JTable {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private JTable scoreTable;
    /**
     * 表格部分
     */
    private void initTableUI() {
        scoreTable = new SHTable();
        //设置表头颜色
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBackground(new Color(204, 204, 210, 247));
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        scoreTable.getTableHeader().setDefaultRenderer(cellRenderer);
//        scoreTable.setRowHeight(90);

        scoreTable.setShowGrid(true);
        scoreTable.setGridColor(Color.black); // 默认是白色
        scoreTable.setDefaultRenderer(Object.class, new TableCellTextAreaRenderer());
//        scoreTable.setCellSelectionEnabled(true);

        //如果 JTbale 对象直接添加到 JFrame 中，则表头显示不出来，需要把表格对象放入 JScrollPane 对象中
        add(new JScrollPane(scoreTable), BorderLayout.CENTER);
    }

    private void initEvents() {
        //读取文件事件
        inputBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int state; //文件选择器返回状态
                state = chooser.showOpenDialog(null); //显示打开文件对话框

                file = chooser.getSelectedFile(); //获得文件

                if (file != null && state == JFileChooser.APPROVE_OPTION) { //选择了文件并点击了打开按钮
                    try {
                        scanFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (state == JFileChooser.CANCEL_OPTION) { //撤销

                } else if (state == JFileChooser.ERROR_OPTION) {
                    JOptionPane.showMessageDialog(null, "错误!");
                }

            }
        });

        //开始分析
        analyseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content = inputArea.getText();
                try {
                    startAnalyze(content);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        //保存结果
        outputBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveResult();
                    JOptionPane.showMessageDialog(null, "保存成功");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //扫描文件
    private void scanFile() throws IOException {
        String path = file.getPath().toString();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        // 删除最后一个新行分隔符
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();
        content = stringBuilder.toString();
        inputArea.setText(content);
    }

    //保存结果
    private void saveResult() throws IOException {
        chooser.showSaveDialog(null);
        FileOutputStream fos = null;
        File f=chooser.getSelectedFile();
        String fname = f.getName();
        File temp = new File(chooser.getCurrentDirectory()+"/"+fname+".txt");
        fos = new FileOutputStream(temp);
        //写入文件操作
        fos.write(result.getBytes());
        fos.close();
    }

//    /**
//     * 符号表
//     */
//    private void showTable() {
//        HashSet<String> terminals = new HashSet<>(lr0Parser.getGrammar().getTerminals());//终结符集
//        terminals.add("$");
//        HashSet<String> variables = lr0Parser.getGrammar().getVariables(); //非终结符集
//        HashMap<String, Action>[] actionTable = lr0Parser.getActionTable(); //分析表终结符部分
//        HashMap<String, Integer>[] gotoTable = lr0Parser.getGoToTable(); //分析表非终结符部分
//
//        System.out.print("\t");
//        for (String terminal : terminals) {
//            //第一行，显示全部终结符
//            System.out.print(terminal + "\t");
//        }
//        for (String variable : variables) {
//            //第一行，显示全部非终结符
//            System.out.print(variable + "\t");
//        }
//        System.out.print("\n");
//
//        //第二行到最后一行 一行一个状态
//        for (int state = 0; state < actionTable.length; state++) {
//            System.out.print(state + "\t");
//            for (String terminal : terminals) {
//                String text = actionTable[state].get(terminal) == null ? "" : actionTable[state].get(terminal).toString();
//                System.out.print(text + "\t");
//            }
//            for (String variable : variables) {
//                String text = gotoTable[state].get(variable) == null ? "" : gotoTable[state].get(variable).toString();
//                System.out.print(text + "\t");
//            }
//            System.out.print("\n");
//        }
//    }

    private void initParser() {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null; // 保存分析表 方便出现错误时查看
        String rulesInput = "";
        try {
            inputStream = new FileInputStream("grammar.txt");
            rulesInput = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            outputStream = new FileOutputStream("table.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        lr0Parser = new LR0Parser(new Grammar(rulesInput));
        if (lr0Parser.parserSLR1()) {
            System.out.println(lr0Parser.actionTableStr());
            System.out.println(lr0Parser.goToTableStr());
            try {
                outputStream.write(lr0Parser.actionTableStr().getBytes());
                outputStream.write(lr0Parser.goToTableStr().getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("parse not ok");
            System.out.println(lr0Parser.canonicalCollectionStr());
        }
    }

    /**
     * 分析程序
     * @param sourceCode 程序文本
     */
    private void startAnalyze(String sourceCode) throws IOException {
        Lexer lexer = new Lexer(sourceCode); // 词法分析器
        StringBuilder builder = new StringBuilder(); //存词法分析显示结果
        ArrayList<String> input = new ArrayList<>(); //词法分析结果（满足语法分析的格式）
        // 识别词法单元
        while (true) {
            Token token = lexer.scan();
            if (token.tag == Tag.CODE_END) {
                break;
            } else if (token.tag != Tag.ERROR) {
                input.add(token.desc());
                builder.append(token + "\n");
            }
        }

        builder.append("\n\n错误信息：\n");
        for (LexError error : lexer.errors) {
            builder.append(error);
            builder.append("\n");
        }
        builder.append("\n符号表：\b");
        for (Word word : lexer.wordList) {
            builder.append(word);
            builder.append("\n");
        }
        result = builder.toString();
        outputArea.setText(builder.toString());

        // 语法分析
        boolean success = lr0Parser.accept(input);
        var result = lr0Parser.getResult();
        if(!success) {
            List<String> e = new ArrayList<>();
            e.add(""); e.add(""); e.add("失败");
            result.add(e);
            System.out.println("语法分析 失败");
            System.out.println(lr0Parser.canonicalCollectionStr());
        }

        DefaultTableModel tableModel=(DefaultTableModel) scoreTable.getModel();    //获得表格模型
        tableModel.setRowCount(0);    //清空表格中的数据
        tableModel.setColumnIdentifiers(new Object[]{"状态栈","符号栈", "操作"});    //设置表头

        for (List<String> operation : result) {
            tableModel.addRow(new Object[]{operation.get(0), operation.get(1), operation.get(2)});    //增加行
        }

        scoreTable.setModel(tableModel);    //应用表格模型

    }

}


class TableCellTextAreaRenderer extends JTextArea implements TableCellRenderer {
    public TableCellTextAreaRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        // 计算当下行的最佳高度
        int maxPreferredHeight = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            setText("" + table.getValueAt(row, i));
            setSize(table.getColumnModel().getColumn(column).getWidth(), 0);
            maxPreferredHeight = Math.max(maxPreferredHeight, getPreferredSize().height);
        }

        if (table.getRowHeight(row) != maxPreferredHeight)  // 少了这行则处理器瞎忙
            table.setRowHeight(row, maxPreferredHeight);

        setText(value == null ? "" : value.toString());
        return this;
    }
}