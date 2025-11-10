package com.chengzhi.example.provider;

import com.chengzhi.chengrpc.RpcApplication;
import com.chengzhi.chengrpc.bootstrap.ProviderBootstrap;
import com.chengzhi.chengrpc.config.RegistryConfig;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.model.ServiceMetaInfo;
import com.chengzhi.chengrpc.model.ServiceRegisterInfo;
import com.chengzhi.chengrpc.registry.LocalRegistry;
import com.chengzhi.chengrpc.registry.Registry;
import com.chengzhi.chengrpc.registry.RegistryFactory;
import com.chengzhi.chengrpc.server.HttpServer;
import com.chengzhi.chengrpc.server.VertxHttpServer;
import com.chengzhi.chengrpc.server.tcp.VertxTcpServer;
import com.chengzhi.example.common.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class ProviderExample {
    public static void main(String[] args) {
        //要注册的服务
        List<ServiceRegisterInfo> serviceMetaInfoList = new ArrayList<>();
        ServiceRegisterInfo serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
        serviceMetaInfoList.add(serviceRegisterInfo);

        //服务提供者初始化
        ProviderBootstrap.init(serviceMetaInfoList);
    }
}
