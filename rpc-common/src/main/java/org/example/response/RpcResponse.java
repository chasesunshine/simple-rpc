package org.example.response;

import lombok.Data;

import java.io.Serializable;

// RpcResponse.java
@Data
public class RpcResponse implements Serializable {
    private String requestId;
    private Object result;
    private Throwable error;

    // getters and setters...
}
