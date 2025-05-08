package org.example;

import org.example.client.RpcClientProxy;
import org.example.service.HelloService;
import org.example.service.ServiceRegistry;
import org.example.service.impl.LocalServiceRegistry;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
public class RpcClientApplication {
//    public static void main(String[] args) {
//        //启动SpringBoot程序
//        SpringApplication.run(RpcClientApplication.class, args);
//    }

    public static void main(String[] args) {
        ServiceRegistry registry = new LocalServiceRegistry();
        RpcClientProxy proxy = new RpcClientProxy(registry);

        HelloService helloService = proxy.createProxy(HelloService.class);
        String result = helloService.sayHello("World");

        System.out.println(result);  // 输出: Hello, World
    }
}