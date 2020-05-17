package dao;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 获取数据库连接
 *
 * @author York
 * @date 2018-11-30 15:08:58
 */

public class Connections {
    private static final String URL = "yourURL";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String USER = "yourUSERNAME";
    private static final String PASSWD = "youPASSWORD";
    private Connection conn = null;

    /**
     * 加载驱动
     */
    public Connections() {
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接
     *
     * @return
     */
    public Connection getCon() {
        return conn;
    }
}
