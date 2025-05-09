# 手写简单的RPC通信框架（Java实现）

下面我将展示一个简单的RPC框架实现，包含服务提供者(Provider)、消费者(Consumer)、注册中心(Registry)和网络传输等基本组件。

## 1. 项目结构

```
simple-rpc/
├── rpc-client/       // 客户端
├── rpc-common/       // 公共类
├── rpc-core/        // 核心实现
├── rpc-server/      // 服务端
└── example/         // 示例
```

## 2. 核心代码实现

### 2.1 公共模块 (rpc-common)

#### 请求/响应对象

```java
// RpcRequest.java
public class RpcRequest implements Serializable {
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    
    // getters and setters...
}

// RpcResponse.java
public class RpcResponse implements Serializable {
    private String requestId;
    private Object result;
    private Throwable error;
    
    // getters and setters...
}
```

### 2.2 核心模块 (rpc-core)

#### 服务注册接口

```java
public interface ServiceRegistry {
    void register(String serviceName, InetSocketAddress inetSocketAddress);
    InetSocketAddress discoverService(String serviceName);
}
```

#### 简单的本地注册中心实现

```java
public class LocalServiceRegistry implements ServiceRegistry {
    private static final Map<String, InetSocketAddress> SERVICE_MAP = new ConcurrentHashMap<>();
    
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        SERVICE_MAP.put(serviceName, inetSocketAddress);
    }
    
    @Override
    public InetSocketAddress discoverService(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }
}
```

#### 网络传输层

```java
public interface TransportClient {
    RpcResponse send(RpcRequest request);
    void close();
}

public class SocketTransportClient implements TransportClient {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    
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
```

#### 服务端处理器

```java
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
```

#### 服务端

```java
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
                executor.execute(() -> {
                    try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                         ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                        
                        RpcRequest request = (RpcRequest) input.readObject();
                        RpcResponse response = requestHandler.handle(request);
                        output.writeObject(response);
                        output.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 客户端代理

```java
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
```

### 2.3 示例使用

#### 定义服务接口

```java
public interface HelloService {
    String sayHello(String name);
}
```

#### 服务端实现

```java
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}

public class ServerExample {
    public static void main(String[] args) {
        ServiceRegistry registry = new LocalServiceRegistry();
        RpcServer server = new RpcServer(registry, 8080);
        
        // 注册服务
        server.registerService(HelloService.class, new HelloServiceImpl());
        
        // 启动服务
        server.start();
    }
}
```

#### 客户端调用

```java
public class ClientExample {
    public static void main(String[] args) {
        ServiceRegistry registry = new LocalServiceRegistry();
        RpcClientProxy proxy = new RpcClientProxy(registry);
        
        HelloService helloService = proxy.createProxy(HelloService.class);
        String result = helloService.sayHello("World");
        
        System.out.println(result);  // 输出: Hello, World
    }
}
```

## 3. 框架优化方向

这个简单实现有很多可以改进的地方：

1. **序列化**：使用更高效的序列化方式（如Protobuf、Kryo）
2. **通信协议**：设计更完善的通信协议
3. **连接池**：实现客户端连接池
4. **负载均衡**：支持多个服务提供者时的负载均衡
5. **服务治理**：增加熔断、降级、限流等机制
6. **异步调用**：支持异步RPC调用
7. **注册中心**：实现基于ZooKeeper/Nacos的注册中心

这个简单框架演示了RPC的核心原理，实际生产环境中的RPC框架（如Dubbo、gRPC）会更加复杂和完善。