package Main.Nodes;

import Main.ExceptionHandlers.VariableInitializationException;
import Main.Nodes.ASTNodes.LiteralNode;

public class AssignmentValidator {
    public static void validateAssignmentType(String dataType, String variable, LiteralNode valueNode) throws VariableInitializationException {
        if (valueNode != null) {
            Object assignedValue = valueNode.getValue();
            if (assignedValue != null) {
                // Check if the assigned value matches the data type
                switch (dataType) {
                    case "INT" -> {
                        try {
                            int intValue = Integer.parseInt(String.valueOf(assignedValue));
                            // The assigned value can be parsed as an int, so it matches the data type INT
                        } catch (NumberFormatException e) {
                            throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' is not a valid integer.");
                        }
                    }
                    case "CHAR" -> {
                        if (!(assignedValue instanceof Character)) {
                            throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' does not match data type CHAR.");
                        }
                    }
                    case "BOOL" -> {
                        if (!(assignedValue.equals("FALSE") || assignedValue.equals("TRUE"))) {
                            throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' does not match data type BOOL.");
                        }
                    }
                    case "FLOAT" -> {
                        try {
                            double floatValue = Double.parseDouble(String.valueOf(assignedValue));
                            // The assigned value can be parsed as a double, so it matches the data type FLOAT
                        } catch (NumberFormatException e) {
                            throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' is not a valid float.");
                        }
                    }
                    case null, default ->
                            throw new VariableInitializationException("ERROR: Unsupported data type '" + dataType + "'.");
                }
            }
        }
    }
}
