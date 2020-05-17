package server;


import com.alibaba.fastjson.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * 服务器端主处理进程
 *
 * @author York
 * @data 2018-11-28 13:02:39
 */
public class ServerHandler extends Thread {
    Socket socket;
    /**
     * 用于共享当前在线用户的输出
     */
    private HashMap<String, DataOutputStream> userOut;

    public ServerHandler(Socket socket, HashMap hashMap) {
        this.socket = socket;
        this.userOut = hashMap;
    }

    @Override
    public void run() {
        DataInputStream dis;
        String message;
        JSONObject jsonObject;
        String request;
        String username = null;
        LoginServer lS = null;
        while (!socket.isClosed()) {
            try {
                dis = new DataInputStream(socket.getInputStream());
                message = dis.readUTF();
                jsonObject = JSONObject.parseObject(message);
                request = jsonObject.getString("request");
                username = jsonObject.getString("username");
                System.out.println(message);
                switch (request) {
                    case "login": {
                        lS = new LoginServer(socket, jsonObject, userOut);
                        lS.start();
                        break;
                    }
                    case "chatWithFriend": {
                        new ChatServer(socket, jsonObject, userOut).start();
                        break;
                    }
                    case "addFriend": {
                        new AddFriServer(socket, jsonObject, userOut).start();
                        break;
                    }
                    case "transFile": {
                        new FileTransServer(socket, jsonObject, userOut).start();
                        break;
                    }
                    case "deleteFriend": {
                        new DelFriServer(socket, jsonObject, userOut).start();
                        break;
                    }
                    default: {
                        System.out.println("指令出错");
                        break;
                    }
                }
            } catch (IOException e) {
                try {
                    lS.updateOnlineFriendList(false);
                    userOut.remove(username);
                    socket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
