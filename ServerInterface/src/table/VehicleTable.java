package table;

import java.io.Serializable;

public class VehicleTable implements Serializable {
    private String carState;
    private String carNumber;

    public VehicleTable(String carState, String carNumber) {
        this.carState = carState;
        this.carNumber = carNumber;
    }

    public String getCarState() {
        return carState;
    }

    public void setCarState(String carState) {
        this.carState = carState;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }
}
