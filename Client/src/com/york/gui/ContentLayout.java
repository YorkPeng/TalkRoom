package com.york.gui;

import util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * 初始化历史消息记录窗口
 * @author York
 * @date 2018-12-2 21:51:28
 */
public class ContentLayout extends JFrame implements Runnable{
    private JLabel jLabelName;
    private JButton deleteButton;
    private JScrollPane jsp;
    private JPanel jpJsp, panel, panelN;
    private JTextArea textAreaOutput;
    private String username, friName;
    private File file;
    private FileReader fr;
    private BufferedReader br;
    private Font font;

    public ContentLayout(String username, String friName){
        this.username = username;
        this.friName = friName;
        file = new File(".\\src\\resource\\"+ username + "_" + friName +  ".txt");
        font = new Font("宋体", Font.BOLD, 20);
        try {
            if (!file.exists()){
                JOptionPane.showMessageDialog(null, "消息记录文件不存在！", "温馨提示", JOptionPane.ERROR_MESSAGE);
                return;
            }
            fr = new FileReader(file);
            br = new BufferedReader(fr);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void makeLayout(){
        if (br == null){
            return;
        }
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("查看历史消息");
        panelN = new JPanel();
        jLabelName = new JLabel("查看与" + friName + "的聊天记录");
        deleteButton = new JButton("删除所有聊天记录");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileWriter fw = new FileWriter(file);
                    fw.write("");
                    fw.flush();
                    fw.close();
                    textAreaOutput.setText("");
                }catch (IOException ee){
                    ee.printStackTrace();
                }
            }
        });
        panelN.add(jLabelName);
        panelN.add(deleteButton);
        this.add(panelN, BorderLayout.NORTH);

        panel = new JPanel();
        final JTabbedPane tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.addTab("消息记录", null, panel, "");
        final CardLayout cl = new CardLayout();
        contentCard();
        panel.setLayout(cl);
        this.setSize(800,500);
        ImageIcon icon1=new ImageIcon(".//src//resource//chat.png");
        this.setIconImage(icon1.getImage());
        this.setFont(font);
        new Util().setLocation(this);
        toFront();
        this.setVisible(true);
    }

    private void contentCard(){
        jpJsp = new JPanel(new GridLayout(5,1));
        jsp = new JScrollPane(jpJsp);
        textAreaOutput = new JTextArea(500,50);
        Font font = new Font("宋体", Font.BOLD, 20);
        textAreaOutput.setFont(font);
        textAreaOutput.setEditable(false);
        try {
            String str = br.readLine();
            String end = "";
            while ((str != null)){
                end = end +str + "\n";
                str = br.readLine();
            }
            textAreaOutput.setText(end);
        }catch (IOException e){
            e.printStackTrace();
        }
        jpJsp.add(textAreaOutput);
        jsp.setBounds(1,35,150,150);
        panel.add(jsp);
    }
    @Override
    public void run(){
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            makeLayout();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
