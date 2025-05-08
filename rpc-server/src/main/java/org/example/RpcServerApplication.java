package org.example;

import org.example.server.RpcServer;
import org.example.service.HelloService;
import org.example.service.ServiceRegistry;
import org.example.service.impl.HelloServiceImpl;
import org.example.service.impl.LocalServiceRegistry;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
public class RpcServerApplication {
//    public static void main(String[] args) {
//        //启动SpringBoot程序
//        SpringApplication.run(RpcServerApplication.class, args);
//    }

    public static void main(String[] args) {
        ServiceRegistry registry = new LocalServiceRegistry();
        RpcServer server = new RpcServer(registry, 9080);

        // 注册服务
        server.registerService(HelloService.class, new HelloServiceImpl());

        // 启动服务
        server.start();
    }
}