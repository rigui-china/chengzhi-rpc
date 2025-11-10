package com.chengzhi.chengrpc.bootstrap;

import com.chengzhi.chengrpc.RpcApplication;
import com.chengzhi.chengrpc.config.RegistryConfig;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.model.ServiceMetaInfo;
import com.chengzhi.chengrpc.model.ServiceRegisterInfo;
import com.chengzhi.chengrpc.registry.LocalRegistry;
import com.chengzhi.chengrpc.registry.Registry;
import com.chengzhi.chengrpc.registry.RegistryFactory;
import com.chengzhi.chengrpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class ProviderBootstrap {
    public static void init(List<ServiceRegisterInfo> serviceRegisterInfoList) {
        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();
        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.registerService(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        // 启动服务器
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }
}
