package server;

import msg.ScreenLock;

public class UserMetaData {
    public ScreenLock lock;
    public String id;
    public String gos_num;

    public UserMetaData(ScreenLock lock) {
        this.lock = lock;
    }

    public UserMetaData(String gos_num) {
        this.gos_num = gos_num;
    }

    public UserMetaData(ScreenLock lock, String gos_num) {
        this.lock = lock;
        this.gos_num = gos_num;
    }

    public UserMetaData(ScreenLock lock, String id, String gos_num) {
        this.lock = lock;
        this.id = id;
        this.gos_num = gos_num;
    }
}
