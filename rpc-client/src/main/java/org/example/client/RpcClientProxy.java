package org.example.client;


import org.example.request.RpcRequest;
import org.example.response.RpcResponse;
import org.example.service.ServiceRegistry;
import org.example.transfer.SocketTransportClient;
import org.example.transfer.TransportClient;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;

public class RpcClientProxy {
    private ServiceRegistry serviceRegistry;
    private TransportClient transportClient;

    public RpcClientProxy(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.transportClient = new SocketTransportClient();
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(interfaceClass.getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParameters(args);

                    InetSocketAddress address = serviceRegistry.discoverService(interfaceClass.getName());
                    if (address == null) {
                        throw new RuntimeException("服务未找到: " + interfaceClass.getName());
                    }

                    transportClient = new SocketTransportClient(address.getHostName(), address.getPort());
                    RpcResponse response = transportClient.send(request);

                    if (response.getError() != null) {
                        throw response.getError();
                    }

                    return response.getResult();
                });
    }
}
