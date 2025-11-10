package com.chengzhi.example.provider;

/**
 * @author 徐晟智
 * @version 1.0
 */

import com.chengzhi.chengrpc.RpcApplication;
import com.chengzhi.chengrpc.registry.LocalRegistry;
import com.chengzhi.chengrpc.server.HttpServer;
import com.chengzhi.chengrpc.server.VertxHttpServer;
import com.chengzhi.example.common.service.UserService;

/**
 * 建议服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        //RPC 框架初始化
        RpcApplication.init();

        //注册服务
        LocalRegistry.registerService(UserService.class.getName(), UserServiceImpl.class);

        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
