package Main.Nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Map<String, VariableSymbol> symbols = new HashMap<>();
    private final Stack<Map<String, VariableSymbol>> scopes = new Stack<>();

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        scopes.pop();
    }

    public void addVariable(String name, String dataType) {
        if (symbols.containsKey(name)) {
            // Variable redeclaration error
        } else {
            VariableSymbol symbol = new VariableSymbol(name, dataType);
            symbols.put(name, symbol);
            scopes.peek().put(name, symbol);
        }
    }

    public void setVariableValue(String name, String scope, Object value) {
        VariableSymbol symbol = symbols.get(name);
        if (symbol != null) {
            symbol.setValue(scope, value);
        }
    }

    public Object getVariableValue(String name, String scope) {
        VariableSymbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol.getValue(scope);
        }
        // Variable not found error
        return null;
    }

    public void printTable() {
        System.out.println("Symbol Table:");
        System.out.println("-------------");
        Map<String, VariableSymbol> currentScope = scopes.peek();
        for (Map.Entry<String, VariableSymbol> entry : currentScope.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getDataType());
        }
        System.out.println("-------------");
    }
}
