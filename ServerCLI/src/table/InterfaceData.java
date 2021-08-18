package table;

import java.io.Serializable;

public class InterfaceData implements Serializable {
    private int lock;
    private int numOfCars;
    private String regNum;

    public int getLock() {
        return lock;
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public int getNumOfCars() {
        return numOfCars;
    }

    public void setNumOfCars(int numOfCars) {
        this.numOfCars = numOfCars;
    }

    public String getRegNum() {
        return regNum;
    }

    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }

    public InterfaceData(int lock, int numOfCars, String regNum) {
        this.lock = lock;
        this.numOfCars = numOfCars;
        this.regNum = regNum;
    }
}
