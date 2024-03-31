package Main.Nodes;

import java.util.HashMap;
import java.util.Map;

public class VariableSymbol {
    private final String name;
    private final String dataType;
    private final Map<String, Object> values; // Store values for different scopes

    public VariableSymbol(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
        this.values = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public Object getValue(String scope) {
        return values.get(scope);
    }

    public void setValue(String scope, Object value) {
        values.put(scope, value);
    }
}