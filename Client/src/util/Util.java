package util;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 工具类
 * @author York
 * @date 2018-12-2 22:10:37
 */
public class Util {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private FileWriter fw;
    public void writeFile(String username, String friendName, String content, boolean flag){
        File file = new File(".\\src\\resource\\"+ username + "_" + friendName +  ".txt");
        try {
            if (!file.exists()){
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            if (flag == true) {
                fw.write(username + "    " + dateFormat.format(new Date()) + "\n" + content + "\n");
                fw.flush();
            }else {
                fw.write(friendName + "    " + dateFormat.format(new Date()) + "\n" + content + "\n");
                fw.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setLocation(Frame frame){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) screenSize.getWidth() / 2 - frame.getWidth() / 2;
        int y = (int) screenSize.getHeight() / 2 - frame.getHeight() / 2;
        frame.setLocation(x, y);
    }
}
