package msg;

import table.SummaryTable;

import java.io.Serializable;
import java.util.ArrayList;

public class AdminMsg implements Serializable {
    public boolean notify;
    public ArrayList<String>carList;
    public ArrayList<SummaryTable>arrayList;

    public AdminMsg(boolean notify, ArrayList<String> carList, ArrayList<SummaryTable> arrayList) {
        this.notify = notify;
        this.carList = carList;
        this.arrayList = arrayList;
    }
}
