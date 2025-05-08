package org.example.service;

import com.azul.crs.com.fasterxml.jackson.core.JsonProcessingException;

import java.net.InetSocketAddress;

public interface ServiceRegistry {

    void register(String serviceName, InetSocketAddress inetSocketAddress);

    InetSocketAddress discoverService(String serviceName) throws JsonProcessingException;
}
