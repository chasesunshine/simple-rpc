package org.example.transfer;

import org.example.request.RpcRequest;
import org.example.response.RpcResponse;

public interface TransportClient {
    RpcResponse send(RpcRequest request);
    void close();
}