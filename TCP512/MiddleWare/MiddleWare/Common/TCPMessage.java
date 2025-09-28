package MiddleWare.Common;

import java.io.Serializable;
import java.util.Vector;

/**
 * TCPMessage - A serializable message container for TCP communication
 *
 * This class encapsulates all communication between clients, middleware, and resource managers.
 * It contains an operation name and an array of parameters for that operation.
 */
public class TCPMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String operation;
    private Object[] parameters;

    /**
     * Constructor for creating a TCPMessage
     * @param operation The operation name (e.g., "addFlight", "queryFlight")
     * @param parameters Variable number of parameters for the operation
     */
    public TCPMessage(String operation, Object... parameters) {
        this.operation = operation;
        this.parameters = parameters;
    }

    /**
     * Get the operation name
     * @return The operation string
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Get all parameters
     * @return Array of parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Get a specific parameter by index
     * @param index The parameter index
     * @return The parameter object, or null if index is invalid
     */
    public Object getParameter(int index) {
        if (parameters != null && index >= 0 && index < parameters.length) {
            return parameters[index];
        }
        return null;
    }

    /**
     * Get the number of parameters
     * @return Parameter count
     */
    public int getParameterCount() {
        return parameters != null ? parameters.length : 0;
    }

    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TCPMessage{operation='").append(operation).append("'");
        if (parameters != null && parameters.length > 0) {
            sb.append(", parameters=[");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(parameters[i]);
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}