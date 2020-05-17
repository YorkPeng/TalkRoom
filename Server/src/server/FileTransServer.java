package server;

import com.alibaba.fastjson.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * 文件传输服务端
 * @author York
 * @date 2018-12-6 12:59:23
 */
public class FileTransServer extends Thread {
    private Socket socket;
    private JSONObject jsonObject;
    private String fileName, fileLength, jsonString, username, friendName;
    private DataInputStream dis;
    private HashMap<String, DataOutputStream> userOut;

    public FileTransServer(Socket socket, JSONObject jsonObject, HashMap hashMap) {
        this.socket = socket;
        this.jsonObject = jsonObject;
        this.userOut = hashMap;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        fileName = jsonObject.getString("fileName");
        fileLength = jsonObject.getString("fileLength");
        friendName = jsonObject.getString("receiver");
        username = jsonObject.getString("sender");
        JSONObject friJson = new JSONObject();
        friJson.put("response", "getFile");
        friJson.put("fileName", fileName);
        friJson.put("fileLength", fileLength);
        friJson.put("fileContent", jsonObject.getString("fileContent"));
        String jsonString = friJson.toJSONString();
        DataOutputStream friDos = userOut.get(friendName);
        try {
            friDos.writeUTF(jsonString);
            friDos.flush();
            friJson.put("response", "sendChatContentToOthers");
            friJson.put("sender", username);
            friJson.put("getter", friendName);
            friJson.put("chatContent", "您已成功接收文件名为" + fileName + "的文件，请前往D：\\fileCache查收");
            String friJsonString = friJson.toJSONString();
            friDos.writeUTF(friJsonString);
            friDos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
