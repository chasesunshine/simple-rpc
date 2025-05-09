package org.example.server;

import org.example.handler.RpcRequestHandler;
import org.example.request.RpcRequest;
import org.example.response.RpcResponse;
import org.example.service.ServiceRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ServiceRegistry serviceRegistry;
    private RpcRequestHandler requestHandler = new RpcRequestHandler();
    private int port;

    public RpcServer(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    public <T> void registerService(Class<T> interfaceClass, T service) {
        requestHandler.registerService(interfaceClass.getName(), service);
        serviceRegistry.register(interfaceClass.getName(), new InetSocketAddress(port));
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动...");

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {

                            RpcRequest request = (RpcRequest) input.readObject();
                            RpcResponse response = requestHandler.handle(request);
                            output.writeObject(response);
                            output.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
