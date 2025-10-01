package MiddleWare.Common;

import java.io.Serializable;
import java.util.Vector;

//Serializable TCPMessage object
public class TCPMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String operation;
    private Object[] parameters;

    public TCPMessage(String operation, Object... parameters) {
        this.operation = operation;
        this.parameters = parameters;
    }

    //Get operation name
    public String getOperation() {
        return operation;
    }

    //Get param in Object form, could be Integer or String
    public Object[] getParameters() {
        return parameters;
    }

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