package org.example.request;

import lombok.Data;

import java.io.Serializable;

// RpcRequest.java
@Data
public class RpcRequest implements Serializable {
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    // getters and setters...
}
