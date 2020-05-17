package bean;

/**
 * 用于保存用户信息
 * @author York
 * @date 2018-12-3 19:58:03
 */
public class UserBean {
    private String userName;
    private String userPWD;

    public UserBean(String userName, String userPWD) {
        this.userName = userName;
        this.userPWD = userPWD;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPWD() {
        return userPWD;
    }

    public void setUserPWD(String userPWD) {
        this.userPWD = userPWD;
    }
}
