package com.chengzhi.chengzhirpcspringbootstarter.bootstrap;

import com.chengzhi.chengrpc.RpcApplication;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.server.tcp.VertxTcpServer;
import com.chengzhi.chengzhirpcspringbootstarter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author 徐晟智
 * @version 1.0
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取 EnableRpc 注解的属性值
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");

        //RPC 框架初始化（配置和注册中心）
        RpcApplication.init();

        //全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //启动服务器
        if(needServer){
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        }else{
            log.info("不启动 server");
        }
    }
}
