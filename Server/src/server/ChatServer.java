package server;

import com.alibaba.fastjson.JSONObject;
import dao.Connections;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * 处理聊天事务
 *
 * @author York
 * @date 2018-11-30 15:09:31
 */
public class ChatServer extends Thread {
    private Socket socket;
    private JSONObject jsonObject;
    private DataOutputStream dos;
    private DataInputStream dis;
    private HashMap<String, DataOutputStream> userOut;
    private String friendName, myName;
    private String chatContent;
    private Connections connections;

    public ChatServer(Socket socket, JSONObject jsonObject, HashMap hashMap) {
        this.socket = socket;
        this.jsonObject = jsonObject;
        this.userOut = hashMap;
        connections = new Connections();
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            myName = jsonObject.getString("username");
            friendName = jsonObject.getString("friendName");
            chatContent = jsonObject.getString("chatContent");
            DataOutputStream dosFriend = userOut.get(friendName);
            JSONObject jsonToMe = new JSONObject();
            jsonToMe.put("response", "sendChatContentToMe");
            putJson(jsonToMe, myName, friendName, chatContent);
            JSONObject jsonToOthers = new JSONObject();
            jsonToOthers.put("response", "sendChatContentToOthers");
            putJson(jsonToOthers, myName, friendName, chatContent);
            String jsonToMeString = jsonToMe.toJSONString();
            String jsonToOthersString = jsonToOthers.toJSONString();
            if (dosFriend != null) {
                dos.writeUTF(jsonToMeString);
                dosFriend.writeUTF(jsonToOthersString);
                System.out.println(jsonToOthersString);
            } else {
                jsonToMe.put("response", "sendChatContentToMe");
                jsonToMe.put("sender", "系统提示");
                jsonToMe.put("chatContent", "您的好友没有上线！");
                String errorString = jsonToMe.toJSONString();
                dos.writeUTF(errorString);
                System.out.println(errorString);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putJson(JSONObject jsonObject, String username, String friendName, String chatContent){
        jsonObject.put("sender", username);
        jsonObject.put("getter", friendName);
        jsonObject.put("chatContent", chatContent);
    }
}
