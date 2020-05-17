package com.york.gui;

import com.alibaba.fastjson.JSONObject;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 初始化聊天窗口
 * @author York
 * @date 2018-12-2 21:51:49
 */
public class ChatLayout extends JFrame implements Runnable {
    private DataInputStream dis;
    private DataOutputStream dos;
    private JPanel panel, panel_info;
    private JScrollPane sPane;
    private JTextArea textContent;
    private JLabel lblSend, lblName;
    private JTextField textSend;
    private JButton btnSend, btnCheck, btnSendFile;
    private String chatContent, username, friendName;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private HashMap<String, ChatLayout> chatWindows;

    public ChatLayout(DataInputStream dataInputStream, DataOutputStream dataOutputStream, String friendName, String username, HashMap hashMap){
        this.dis = dataInputStream;
        this.dos = dataOutputStream;
        this.friendName = friendName;
        this.username = username;
        this.chatWindows = hashMap;
        Thread th = new Thread(this);
        th.start();
    }

    private void makeLayout(){
        ImageIcon icon1=new ImageIcon(".//src//resource//chat.png");
        this.setIconImage(icon1.getImage());
        setTitle("与 " + friendName + " 聊天中....");

        lblName = new JLabel(friendName, new ImageIcon(".//src//resource//friendhead_online.png"), JLabel.LEFT);
        btnCheck = new JButton("查询聊天记录");
        btnCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO 新建界面
                getContent();
            }
        });
        panel_info = new JPanel();
        panel_info.add(lblName);
        panel_info.add(btnCheck);

        textContent = new JTextArea();
        //设置文本域只读
        textContent.setEditable(false);
        sPane = new JScrollPane(textContent);
        lblSend = new JLabel("在这里说话:");
        textSend = new JTextField(30);
        btnSend = new JButton("发送");
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SendMessageToServer().start();
            }
        });
        btnSendFile = new JButton("发送文件");
        btnSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "目前此功能只允许发送文本类文件！", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
                Thread th = new Thread(new SendFileToServer());
                th.start();
            }
        });

        panel = new JPanel();
        panel.add(lblSend);
        panel.add(textSend);
        panel.add(btnSend);
        panel.add(btnSendFile);
        this.add(panel_info, BorderLayout.NORTH);
        this.add(panel, BorderLayout.SOUTH);

        this.add(sPane);
        this.setSize(800,500);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatWindows.remove(friendName);
            }
        });
        new Util().setLocation(this);
        toFront();
        this.setVisible(true);
    }

    @Override
    public void run(){
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            makeLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshMsg(JSONObject json){
        String sender = json.getString("sender");
        String content = json.getString("chatContent");
        textContent.append(sender + "    " + dateFormat.format(new Date()) + "\n" + content + "\n");
    }

    private void getContent(){
        Thread th = new Thread(new ContentLayout(username, friendName));
        th.start();
    }

    /**
     * 多线程发送类，采用类内定义
     * @author York
     * @date 2018-12-2 22:13:07
     */

    class SendMessageToServer extends Thread{
        @Override
        public void run(){
            chatContent = textSend.getText();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("request", "chatWithFriend");
            jsonObject.put("username", username);
            jsonObject.put("friendName", friendName);
            jsonObject.put("chatContent", chatContent);
            String jsonString = jsonObject.toJSONString();
            try {
                dos.writeUTF(jsonString);
            }catch (IOException e){
                e.printStackTrace();
            }
            new Util().writeFile(username, friendName, chatContent, true);
            textSend.setText("");
        }
    }

    /**
     * 多线程发送文件类，采用类内定义
     * @author York
     * @date 2018-12-2 22:13:33
     */

    class SendFileToServer extends JFrame implements Runnable{
        @Override
        public void run() {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            jfc.showDialog(new JLabel(), "选择");
            this.setIconImage(new ImageIcon(".//src//resource//chat.png").getImage());
            File file = jfc.getSelectedFile();
            if (file != null) {
                if (file.isDirectory()) {
                    JOptionPane.showMessageDialog(null, "请不要选择文件夹！", "错误", JOptionPane.ERROR_MESSAGE);
                } else if (file.isFile()) {
                    String filePath = file.getAbsolutePath();
                    System.out.println("文件:" + filePath);
                    JSONObject tranJson = new JSONObject();
                    tranJson.put("request", "transFile");
                    tranJson.put("fileName", file.getName());
                    tranJson.put("fileLength", (int) file.length());
                    tranJson.put("sender", username);
                    tranJson.put("receiver", friendName);
                    String string = "";
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                        BufferedReader is = new BufferedReader(isr);
                        String a;
                        while ((a = is.readLine()) != null) {
                            string += a;
                        }
                        tranJson.put("fileContent", string);
                        System.out.println(string);
                        String tranJsonString = tranJson.toJSONString();
                        dos.writeUTF(tranJsonString);
                        dos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
