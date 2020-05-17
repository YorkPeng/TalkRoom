package server;

import com.alibaba.fastjson.JSONObject;
import dao.Connections;
import dao.UserDAO;
import util.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class AddFriServer extends Thread {
    private Socket socket;
    private JSONObject jsonObject;
    private HashMap<String, DataOutputStream> userOut;
    private DataOutputStream dos, friDos;

    public AddFriServer(Socket socket, JSONObject jsonObject, HashMap hashMap) {
        this.socket = socket;
        this.jsonObject = jsonObject;
        this.userOut = hashMap;
    }

    @Override
    public void run() {
        Connections connections = new Connections();
        String username = jsonObject.getString("username");
        String friendName = jsonObject.getString("friendName");
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            friDos = userOut.get(friendName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (friDos != null) {
            if (new UserDAO().addFri(connections, username, friendName)) {
                new Util().sendMsg(username, friendName, dos, 1);
                JSONObject friJson = new JSONObject();
                friJson.put("response", "someoneAddU");
                friJson.put("username", username);
                friJson.put("friendName", friendName);
                String friJsonString = friJson.toJSONString();
                try {
                    friDos.writeUTF(friJsonString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                new Util().sendMsg(username, friendName, dos, 0);
            }
        } else {
            new Util().sendMsg(username, friendName, dos, 0);
        }
    }
}
