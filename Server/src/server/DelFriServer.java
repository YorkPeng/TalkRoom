package server;

import com.alibaba.fastjson.JSONObject;
import dao.Connections;
import dao.UserDAO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * 处理删除好友请求
 *
 * @author York
 * @date 2018-12-2 22:15:17
 */
public class DelFriServer extends Thread {
    private Socket socket;
    private JSONObject jsonObject;
    private HashMap<String, DataOutputStream> userOut;
    private DataOutputStream dos;
    private DataInputStream dis;
    private DataOutputStream friDos;

    public DelFriServer(Socket socket, JSONObject jsonObject, HashMap hashMap) {
        this.socket = socket;
        this.jsonObject = jsonObject;
        this.userOut = hashMap;
    }

    @Override
    public void run() {
        String username = jsonObject.getString("username");
        String friendName = jsonObject.getString("friendName");
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            friDos = userOut.get(friendName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Connections connections = new Connections();
        new UserDAO().delFri(connections, username, friendName);
        JSONObject friJson = new JSONObject();
        friJson.put("response", "successDelete");
        friJson.put("username", username);
        friJson.put("friendName", friendName);
        String friJsonString = friJson.toJSONString();
        try {
            friDos.writeUTF(friJsonString);
            System.out.println("OK?");
            friDos.flush();
        } catch (IOException ee) {
            ee.printStackTrace();
        }
    }

}
