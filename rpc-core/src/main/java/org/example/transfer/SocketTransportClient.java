package org.example.transfer;

import org.example.request.RpcRequest;
import org.example.response.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketTransportClient implements TransportClient {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public SocketTransportClient(){
    }
    public SocketTransportClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse send(RpcRequest request) {
        try {
            connect();

            outputStream.writeObject(request);
            outputStream.flush();

            return (RpcResponse) inputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("RPC调用失败", e);
        }
    }

    private void connect() throws IOException {
        if (socket == null) {
            // 如果这行代码没有抛出异常（如IOException），则表示TCP三次握手已完成，客户端与服务端（localhost:8080）的连接已成功建立。
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        }
    }

    @Override
    public void close() {
        // 关闭资源...
    }
}
