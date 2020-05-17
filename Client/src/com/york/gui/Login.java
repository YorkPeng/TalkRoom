package com.york.gui;

import com.alibaba.fastjson.JSONObject;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 形成登陆界面，主要实现了把用户输入的数据发送到S端
 * @author York
 * @date 2018-12-2 22:08:07
 */
public class Login extends JFrame implements Runnable{
    /**
     * 基础控件
     */
    private static JButton jButton_login;
    private static JFrame jFrame1;
    private static JLabel jLabel1;
    private static JTextField jTextField_username;
    private static JPasswordField jPasswordField_password;
    private static JLabel jLabel_username;
    private static JLabel jLabel_password;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private JSONObject json;

    public Login(){
        try {
            socket = new Socket("127.0.0.1",8083);
        }catch (IOException e){
            e.printStackTrace();
        }
        Font font = new Font("宋体", Font.PLAIN, 20);
        jFrame1 = new JFrame("登录界面");
        jLabel1 = new JLabel();

        jLabel_username = new JLabel("用户名:");
        jLabel_username.setBounds(20,50,80,50);
        jLabel_username.setFont(font);

        jLabel_password = new JLabel("密码:");
        jLabel_password.setBounds(20,120,80,50);
        jLabel_password.setFont(font);

        jButton_login = new JButton("登陆");
        jButton_login.setBounds(190,250,100,50);
        jButton_login.setFont(font);

        jTextField_username = new JTextField();
        jTextField_username.setBounds(180,50,250,50);
        jTextField_username.setFont(font);

        jPasswordField_password = new JPasswordField();
        jPasswordField_password.setBounds(180,120,250,50);
        jPasswordField_password.setFont(font);

        jLabel1.add(jTextField_username);
        jLabel1.add(jPasswordField_password);
        jLabel1.add(jLabel_username);
        jLabel1.add(jLabel_password);
        jLabel1.add(jButton_login);

        jFrame1.add(jLabel1);
        new Util().setLocation(this);
        jFrame1.setVisible(true);
        ImageIcon icon=new ImageIcon(".//src//resource//chat.png");
        jFrame1.setIconImage(icon.getImage());
        jFrame1.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame1.setSize(500,400);
        toFront();
    }

    @Override
    public void run(){
        json = new JSONObject();
        try {
            //将Metal风格改成Nimbus风格
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
        jButton_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    String username = jTextField_username.getText();
                    String userPWD = String.valueOf(jPasswordField_password.getPassword());
                    if ("".equals(username) || "".equals(userPWD)){
                        JOptionPane.showMessageDialog(null, "用户名或密码不能为空", "登陆错误", JOptionPane.ERROR_MESSAGE);
                    }else {
                        json.put("request", "login");
                        json.put("username", username);
                        json.put("password", userPWD);
                        String jsonString = json.toJSONString();
                        System.out.println(username);
                        System.out.println(userPWD);
                        try {
                            dos.writeUTF(jsonString);
                            String flag = dis.readUTF();
                            if ("false".equals(flag)) {
                                JOptionPane.showMessageDialog(null, "用户名或密码错误或此用户已在线", "登陆错误", JOptionPane.ERROR_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, "登陆成功", "登陆成功", JOptionPane.INFORMATION_MESSAGE);
                                new MainLayout(dis, dos, socket);
                                jFrame1.dispose();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
        });
    }

    public static void main(String []args){
        Thread th = new Thread(new Login());
        th.start();
    }
}
