package server;

import bean.UserBean;
import com.alibaba.fastjson.JSONObject;
import dao.Connections;
import dao.UserDAO;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.HashMap;


/**
 * 处理登陆的事务
 *
 * @author York
 * @date 2018-11-30 15:08:02
 */
public class LoginServer extends Thread {
    private String userPWD;
    private String userName;
    private UserBean user;
    private Socket client;
    private JSONObject jsonObject;
    private DataOutputStream dos;
    private HashMap<String, DataOutputStream> userOut;
    private Connections connections;
    private ResultSet rs;

    public LoginServer(Socket socket, JSONObject object, HashMap userOut) {
        this.client = socket;
        jsonObject = object;
        this.userOut = userOut;
        connections = new Connections();
        try {
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(JSONObject jsonObject) {
        userName = jsonObject.getString("username");
        userPWD = jsonObject.getString("password");
        user = new UserBean(userName, userPWD);
    }

    public boolean isLogin(UserBean user) {
        return new UserDAO().isMember(connections, user);
    }

    public void updateOnlineFriendList(Boolean flag) {
        String friendName;
        DataOutputStream friDos;
        JSONObject updateOnline = new JSONObject();
        String jsonString;
        JSONObject friListJson = new JSONObject();
        rs = new UserDAO().isMyFri(connections, user);
        if (flag == true) {
            try {
                String myName;
                while (rs.next()) {
                    myName = rs.getString("username");
                    friendName = rs.getString("friendname");
                    if (userOut.containsKey(friendName)) {
                        friListJson.put(friendName, "1");
                        friDos = userOut.get(friendName);
                        updateOnline.put("response", "someoneOnline");
                        updateOnline.put("username", myName);
                        updateOnline.put("isOnline", "1");
                        jsonString = updateOnline.toJSONString();
                        friDos.writeUTF(jsonString);
                    } else {
                        friListJson.put(friendName, "0");
                    }
                }
                String listString = friListJson.toJSONString();
                System.out.println(listString);
                dos.writeUTF(listString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String myName;
                while (rs.next()) {
                    myName = rs.getString("username");
                    friendName = rs.getString("friendname");
                    friDos = userOut.get(friendName);
                    if (friDos != null) {
                        updateOnline.put("response", "someoneOffline");
                        updateOnline.put("username", myName);
                        jsonString = updateOnline.toJSONString();
                        friDos.writeUTF(jsonString);
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

    }

    @Override
    public void run() {
        try {
            setUser(jsonObject);
            if (isLogin(getUser()) && !userOut.containsKey(userName)) {
                System.out.println(user.getUserName() + "登陆成功");
                dos.writeUTF("true");
                dos.writeUTF(jsonObject.getString("username"));
                userOut.put(user.getUserName(), dos);
                updateOnlineFriendList(true);
            } else {
                System.out.println("登录失败");
                dos.writeUTF("false");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
