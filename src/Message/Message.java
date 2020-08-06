package Message;

import java.io.Serializable;

public class Message implements Serializable {
    private String name;
    private String method;
    private Class<?>[] Parameterstype;
    private Object[] parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Class<?>[] getParameterstype() {
        return Parameterstype;
    }

    public void setParameterstype(Class<?>[] parameterstype) {
        Parameterstype = parameterstype;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
