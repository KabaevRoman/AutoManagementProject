package msg;

import table.SummaryTable;

import java.io.Serializable;
import java.util.ArrayList;

public class UserMsg implements Serializable {
    private ScreenLock lock;
    private int numOfCars;
    private String regNum;
    private ArrayList<SummaryTable> summaryTable;

    public ScreenLock getLock() {
        return lock;
    }

    public void setLock(ScreenLock lock) {
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

    public ArrayList<SummaryTable> getSummaryTable() {
        return summaryTable;
    }

    public void setSummaryTable(ArrayList<SummaryTable> summaryTable) {
        this.summaryTable = summaryTable;
    }

    public UserMsg(ScreenLock lock, int numOfCars, String regNum, ArrayList<SummaryTable> summaryTable) {
        this.lock = lock;
        this.numOfCars = numOfCars;
        this.regNum = regNum;
        this.summaryTable = summaryTable;
    }
}
