package table;

import java.io.Serializable;
import java.util.Map;

public class ServiceMsg implements Serializable {
    public String command;
    public Map<String, String> parameters;
}
