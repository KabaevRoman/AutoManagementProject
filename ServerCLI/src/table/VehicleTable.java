package table;

import java.io.Serializable;

public class VehicleTable implements Serializable {
    private String regNum;
    private String vehicleState;

    public VehicleTable(String regNum, String vehicleState) {
        this.regNum = regNum;
        this.vehicleState = vehicleState;
    }

    public String getRegNum() {
        return regNum;
    }

    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }

    public String getVehicleState() {
        return vehicleState;
    }

    public void setVehicleState(String vehicleState) {
        this.vehicleState = vehicleState;
    }
}
