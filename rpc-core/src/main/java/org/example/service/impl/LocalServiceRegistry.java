package org.example.service.impl;

import com.azul.crs.com.fasterxml.jackson.core.JsonProcessingException;
import com.azul.crs.com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.ServiceRegistry;
import redis.clients.jedis.Jedis;
//import org.springframework.beans.factory.config.ConfigurableBeanFactory;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@Service
//@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalServiceRegistry implements ServiceRegistry {
//    public static final Map<String, InetSocketAddress> SERVICE_MAP = new ConcurrentHashMap<>();

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        Jedis jedis = new Jedis("localhost", 6379);
        ObjectMapper mapper = new ObjectMapper();
        // 转换为JSON字符串
        String json = null;
        try {
            json = mapper.writeValueAsString(new AddressWrapper(inetSocketAddress));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 设置键值
        jedis.set(serviceName, json);

//        SERVICE_MAP.put(serviceName, inetSocketAddress);
    }

    @Override
    public InetSocketAddress discoverService(String serviceName) {
        Jedis jedis = new Jedis("localhost", 6379);
        String storedJson = jedis.get(serviceName);
        ObjectMapper mapper = new ObjectMapper();
        AddressWrapper wrapper = null;
        try {
            wrapper = mapper.readValue(storedJson, AddressWrapper.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        InetSocketAddress restoredAddress = wrapper.toInetSocketAddress();

        return restoredAddress;

//        return SERVICE_MAP.get(serviceName);
    }

    // 包装类用于JSON序列化
    static class AddressWrapper {
        private String host;
        private int port;

        public AddressWrapper() {}

        public AddressWrapper(InetSocketAddress address) {
            this.host = address.getHostString();
            this.port = address.getPort();
        }

        public InetSocketAddress toInetSocketAddress() {
            return new InetSocketAddress(host, port);
        }

        // getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }
}
