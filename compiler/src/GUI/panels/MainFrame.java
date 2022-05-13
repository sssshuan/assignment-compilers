package GUI.panels;

import lexer.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.logging.Logger;

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

    public MainFrame() throws HeadlessException {
        super("词法分析器");
        chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("标签文件(*.txt)", "txt");
        chooser.setFileFilter(filter);
        initFrame();
        initEvents();
        pack();
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

        //界面添加
        setLayout(new FlowLayout());
        add(inputPanel);
        add(analyseBtn);
        add(outputPanel);
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
                Lexer lexer = new Lexer(content);
                // 开始分割词法单元
                StringBuilder builder = new StringBuilder();
                while (true) {
                    Token token = null;
                    try {
                        token = lexer.scan();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (token.tag == Tag.CODE_END) {
                        break;
                    } else if (token.tag != Tag.ERROR) {
                        builder.append(token);
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
}