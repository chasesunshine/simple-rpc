package org.example.service.impl;

import org.example.server.RpcServer;
import org.example.service.HelloService;
import org.example.service.ServiceRegistry;
//import org.springframework.stereotype.Component;

//import javax.annotation.PostConstruct;

//@Component
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
