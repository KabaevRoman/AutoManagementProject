package msg;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private boolean adminStatus;
    private String username;
    private String password;

    public UserInfo(boolean adminStatus, String username, String password) {
        this.adminStatus = adminStatus;
        this.username = username;
        this.password = password;
    }

    public boolean isAdminStatus() {
        return adminStatus;
    }

    public void setAdminStatus(boolean adminStatus) {
        this.adminStatus = adminStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
