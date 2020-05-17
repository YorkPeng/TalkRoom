package server;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * 用于获取套接字
 *
 * @author York
 * @data 2018-11-28 13:02:39
 */
public class MainServer {

    public MainServer() {
        try {
            HashMap<String, DataOutputStream> userOut = new HashMap<>(10);
            ServerSocket serverSocket = new ServerSocket(8083);
            System.out.println("等待socket连接");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress() + "已经加入了本服务器");
                new ServerHandler(socket, userOut).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MainServer();
    }
}
