package msg;

public enum ScreenLock {
    UNLOCKED(0),
    LOCKED_APPROVED(1),
    LOCKED_DISMISSED(2);
    private final int value;

    ScreenLock(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
