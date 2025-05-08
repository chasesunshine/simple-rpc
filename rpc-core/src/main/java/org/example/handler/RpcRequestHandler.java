package org.example.handler;

import org.example.request.RpcRequest;
import org.example.response.RpcResponse;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHandler {
    private Map<String, Object> registeredServices = new ConcurrentHashMap<>();

    public void registerService(String serviceName, Object service) {
        registeredServices.put(serviceName, service);
    }

    public RpcResponse handle(RpcRequest request) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Object service = registeredServices.get(request.getClassName());
            if (service == null) {
                throw new RuntimeException("服务未找到: " + request.getClassName());
            }

            Method method = service.getClass().getMethod(
                    request.getMethodName(),
                    request.getParameterTypes());

            Object result = method.invoke(service, request.getParameters());
            response.setResult(result);
        } catch (Exception e) {
            response.setError(e);
        }

        return response;
    }
}