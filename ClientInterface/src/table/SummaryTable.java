package table;

import java.io.Serializable;

public class SummaryTable implements Serializable {
    private String idSum;
    private String name;
    private String departureTime;
    private String PDO;
    private String note;
    private Integer gosNum;
    private String arriveTime;


    public SummaryTable(String idSum, String name, String departureTime, String PDO, String note, Integer gosNum, String arriveTime) {
        this.idSum = idSum;
        this.name = name;
        this.departureTime = departureTime;
        this.PDO = PDO;
        this.note = note;
        this.gosNum = gosNum;
        this.arriveTime = arriveTime;
    }

    public String getIdSum() {
        return idSum;
    }

    public String getName() {
        return name;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getPDO() {
        return PDO;
    }

    public String getNote() {
        return note;
    }

    public Integer getGosNum() {
        return gosNum;
    }


    public String getArriveTime() {
        return arriveTime;
    }

    public void setIdSum(String idSum) {
        this.idSum = idSum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setPDO(String PDO) {
        this.PDO = PDO;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setGosNum(Integer gosNum) {
        this.gosNum = gosNum;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }
}