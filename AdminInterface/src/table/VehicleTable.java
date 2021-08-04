package table;

import java.io.Serializable;

public class VehicleTable implements Serializable {
    private String carState;
    private int carNumber;

    public VehicleTable(String carState, int carNumber) {
        this.carState = carState;
        this.carNumber = carNumber;
    }

    public String getCarState() {
        return carState;
    }

    public void setCarState(String carState) {
        this.carState = carState;
    }

    public int getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(int carNumber) {
        this.carNumber = carNumber;
    }
}
