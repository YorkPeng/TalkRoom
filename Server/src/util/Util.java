package util;

import com.alibaba.fastjson.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;

public class Util {

    public void sendMsg(String username, String friendName, DataOutputStream dos, int flag) {
        JSONObject Json = new JSONObject();
        if (flag == 1) {
            Json.put("response", "successAdd");
        } else if (flag == 0) {
            Json.put("response", "notFound");
        } else {
            Json.put("response", "refuseAdd");
        }
        Json.put("friendName", friendName);
        Json.put("username", username);
        String JsonString = Json.toJSONString();
        try {
            dos.writeUTF(JsonString);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
