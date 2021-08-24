package msg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceMsg implements Serializable {
    public String command;
    public Map<String, String> parameters;

    public ServiceMsg() {
        this.parameters = new HashMap<>();
    }
}
