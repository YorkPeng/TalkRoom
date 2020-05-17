package com.york.gui;

import com.alibaba.fastjson.JSONObject;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

/**
 * 形成登陆完成后的主页面，包含好友列表，删除、增加好友以及退出的按钮
 * @author York
 * @date 2018-12-2 22:07:24
 */

public class MainLayout extends JFrame implements Runnable,MouseListener {
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;
    private ImageIcon head;
    private JScrollPane jsp;
    private JPanel jpJsp;
    private JPanel panel5;
    private JSONObject jsonObject;
    private static HashMap<String, ChatLayout> chatWindows = new HashMap<>();
    private Socket socket;
    private JLabel[] jbls;
    private JPopupMenu jpm;
    private JLabel jpmLabelDel;

    public void mainLayout(){
        setTitle("MyChat");
        setBounds(100, 100, 247, 581);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.NORTH);

        final JLabel label = new JLabel("头像",head,JLabel.CENTER);
        panel.add(label, BorderLayout.WEST);
        label.setPreferredSize(new Dimension(100,100));

        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel.add(panel1, BorderLayout.CENTER);

        final JLabel usernameLabel = new JLabel();
        usernameLabel.setText("       " + username);
        panel1.add(usernameLabel, BorderLayout.CENTER);

        final JLabel onlineLabel = new JLabel();
        onlineLabel.setText("       在线");
        panel1.add(onlineLabel, BorderLayout.SOUTH);

        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        getContentPane().add(panel2, BorderLayout.SOUTH);

        final JPanel panel3 = new JPanel();
        final FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panel3.setLayout(flowLayout);
        panel2.add(panel3);

        final JButton button = new JButton();
        panel3.add(button);
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setText("添加好友");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFri();
            }
        });

        final JPanel panel4 = new JPanel();
        panel2.add(panel4, BorderLayout.EAST);

        final JButton button2 = new JButton();
        panel4.add(button2);
        button2.setText("退出");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });

        final JTabbedPane tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        panel5 = new JPanel();
        tabbedPane.addTab("好友列表", null, panel5, "好友列表");
        final CardLayout cl = new CardLayout();
        friendCard();
        panel5.setLayout(cl);

        final FlowLayout flowLayout1 = new FlowLayout();
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        new Util().setLocation(this);
        toFront();
    }

    private void friendCard(){
        jpJsp = new JPanel(new GridLayout(30,1));
        jsp = new JScrollPane(jpJsp);
        String friString = null;
        try {
            friString = dis.readUTF();
        }catch (IOException e){
            e.printStackTrace();
        }
        JSONObject friendListJson = JSONObject.parseObject(friString);
        Set<String> key = friendListJson.keySet();
        System.out.println(key);
        jbls = new JLabel[key.size()];
        int count = 0;
        for (String name : key) {
            if ("1".equals(friendListJson.getString(name))) {
                jbls[count] = new JLabel(name, new ImageIcon(".//src//resource//friendhead_online.png"), JLabel.LEFT);
            } else {
                jbls[count] = new JLabel(name, new ImageIcon(".//src//resource//friendhead_offline.png"), JLabel.LEFT);
            }
            jbls[count].addMouseListener(this);
            jpJsp.add(jbls[count++]);
        }
        jsp.setBounds(1,35,150,150);
        panel5.add(jsp);
    }

    @Override
    public void run() {
        String friendName;
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        }catch (Exception ee){
            ee.printStackTrace();
        }
        String name;
        mainLayout();
        this.setVisible(true);
        while (!socket.isClosed()) {
            try {
                String message = dis.readUTF();
                System.out.println(message);
                jsonObject = JSONObject.parseObject(message);
                String response = jsonObject.getString("response");
                switch (response) {
                    case "someoneOnline": {
                        friendName = jsonObject.getString("username");
                        online(friendName);
                        break;
                    }
                    case "sendChatContentToMe": {
                        name = jsonObject.getString("getter");
                        send(name);
                        break;
                    }
                    case "sendChatContentToOthers": {
                        name = jsonObject.getString("sender");
                        String getter = jsonObject.getString("getter");
                        String content = jsonObject.getString("chatContent");
                        new Util().writeFile(getter, name, content, false);
                        send(name);
                        break;
                    }
                    case "someoneOffline":{
                        friendName = jsonObject.getString("username");
                        offline(friendName);
                        break;
                    }
                    case "getFile":{
                        getFile(jsonObject);
                        break;
                    }
                    case "notFound":{
                        friendName = jsonObject.getString("friendName");
                        JOptionPane.showMessageDialog(null, "找不到名为" + friendName + "的好友或对方不在线", "温馨提示", JOptionPane.WARNING_MESSAGE);
                        break;
                    }
                    case "refuseAdd":{
                        friendName = jsonObject.getString("friendName");
                        JOptionPane.showMessageDialog(null,   friendName + "拒绝了你的好友申请", "温馨提示", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    case "successAdd":{
                        friendName = jsonObject.getString("friendName");
                        JOptionPane.showMessageDialog(null, "您已成功添加  " + friendName + "  为好友", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
                        refreshAddList(friendName);
                        break;
                    }
                    case "successDelete":{
                        friendName = jsonObject.getString("username");
                        System.out.println(friendName);
                        refreshDelList(friendName);
                        break;
                    }
                    case "someoneAddU": {
                        friendName = jsonObject.getString("username");
                        refreshAddList(friendName);
                        break;
                    }
                    default: {
                        System.out.println("指令出错");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
        }
    }

    private void refreshAddList(String friendName){
        JLabel jblAdd = new JLabel(friendName, new ImageIcon(".//src//resource//friendhead_online.png"), JLabel.LEFT);
        jblAdd.addMouseListener(this);
        jpJsp.add(jblAdd);
        jpJsp.revalidate();
    }

    private void refreshDelList(String friendName){
        int count = jpJsp.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component comp = jpJsp.getComponent(i);
            if (comp instanceof JLabel) {
                JLabel friJbl = (JLabel) comp;
                if (friendName.equals(friJbl.getText())){
                    jpJsp.remove(friJbl);
                    System.out.println("OK");
                    jpJsp.revalidate();
                    jpJsp.repaint();
                }
            }
        }
    }

    private void send(String name){
        ChatLayout cLS = chatWindows.get(name);
        if (cLS == null){
            JOptionPane.showMessageDialog(null, "用户 " + name + " 想和你聊天，你没有打开聊天窗 ", "温馨提示", JOptionPane.ERROR_MESSAGE);
            cLS  = new ChatLayout(dis, dos, name, username, chatWindows);
            chatWindows.put(name, cLS);
        }
        try {
            Thread.sleep(500);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        cLS.refreshMsg(jsonObject);
    }

    private void online(String name){
        for (int i = 0; i < jbls.length; i++){
            if (name.equals(jbls[i].getText())){
                jbls[i].setIcon(new ImageIcon(".//src//resource//friendhead_online.png"));
            }
        }
    }

    private void offline(String name){
        for (int i = 0; i < jbls.length; i++){
            if (name.equals(jbls[i].getText())){
                jbls[i].setIcon(new ImageIcon(".//src//resource//friendhead_offline.png"));
            }
        }
    }

    private void addFri(){
        String friendName = JOptionPane.showInputDialog( null,"请输入你要添加的用户名：\n", "添加好友", JOptionPane.INFORMATION_MESSAGE);
        if ("".equals(friendName) || friendName == null) {
            JOptionPane.showMessageDialog(null,    "输入内容为空！", "温馨提示", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int count = jpJsp.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component comp = jpJsp.getComponent(i);
            if (comp instanceof JLabel) {
                JLabel friJbl = (JLabel) comp;
                if (friendName.equals(friJbl.getText())) {
                    JOptionPane.showMessageDialog(null, "该用户已经是你的好友！", "温馨提示", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (friendName.equals(username)){
            JOptionPane.showMessageDialog(null,    "不允许添加自己为好友！", "温馨提示", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JSONObject addJson = new JSONObject();
        addJson.put("request", "addFriend");
        addJson.put("username", username);
        addJson.put("friendName", friendName);
        String addJsonString = addJson.toJSONString();
        try {
            dos.writeUTF(addJsonString);
            dos.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void delFri(String delFriName){
        JSONObject jsonObject =new JSONObject();
        jsonObject.put("request", "deleteFriend");
        jsonObject.put("username", username);
        jsonObject.put("friendName", delFriName);
        String jsonString = jsonObject.toJSONString();
        try {
            dos.writeUTF(jsonString);
            JOptionPane.showMessageDialog(null, "删除好友" + delFriName + "成功！", "温馨提示", JOptionPane.INFORMATION_MESSAGE);
        }catch (IOException ee){
            ee.printStackTrace();
        }
        System.out.println("OK");
        refreshDelList(delFriName);
    }

    private void getFile(JSONObject jsonObject) {
        String savePath = "E:\\fileCache\\";
        String fileName = null;
        BufferedWriter bw = null;
        try {
            fileName = jsonObject.getString("fileName");
            savePath += fileName;
            File file = new File(savePath);
            bw = new BufferedWriter(new FileWriter(file));
            String fileContent = jsonObject.getString("fileContent");
            bw.write(fileContent);
            bw.flush();
            System.out.println("接收完成，文件存为" + savePath);
        }catch (Exception e) {
            System.out.println("接收消息错误");
        }finally {
            try {
                bw.close();
            }catch (IOException ee){
                ee.printStackTrace();
            }
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() == 2) {
                String friendName = ((JLabel) e.getSource()).getText();
                System.out.println(username + "希望和" + friendName + "聊天。");
                ChatLayout chatLayout = new ChatLayout(dis, dos, friendName, username, chatWindows);
                chatWindows.put(friendName, chatLayout);
                return;
            }
        }else if(e.getButton() == MouseEvent.BUTTON3){
            if (e.getClickCount() == 1){
                String delFriName = ((JLabel)e.getSource()).getText();
                System.out.println("你想删除 " + delFriName);
                jpmLabelDel = new JLabel("         " + delFriName);
                jpm = new JPopupMenu("弹出式菜单");
                try {
                    Thread.sleep(300);
                }catch (InterruptedException ee){
                    ee.printStackTrace();
                }
                final JButton jpmButtonDel = new JButton("删除好友");
                jpm.add(jpmLabelDel);
                jpm.add(jpmButtonDel);
                jpm.show(e.getComponent(), e.getX(), e.getY());
                jpmButtonDel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        delFri(delFriName);
                        jpm.remove(jpmLabelDel);
                        jpm.remove(jpmButtonDel);
                    }
                });
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        JLabel jl = (JLabel) e.getSource();
        jl.setForeground(Color.red);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        JLabel jl = (JLabel) e.getSource();
        jl.setForeground(Color.black);
    }

    public MainLayout(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Socket socket){
        try {
            this.dis = dataInputStream;
            this.dos = dataOutputStream;
            this.socket = socket;
            username = dis.readUTF();
        }catch (IOException e1){
            e1.printStackTrace();
        }
        ImageIcon icon1=new ImageIcon(".//src//resource//chat.png");
        head = new ImageIcon(".//src//resource//head.jpg");
        this.setIconImage(icon1.getImage());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Thread th = new Thread(this);
        th.start();
    }
}
