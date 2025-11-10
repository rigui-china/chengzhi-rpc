package com.chengzhi.chengzhirpcspringbootstarter.bootstrap;

import com.chengzhi.chengrpc.RpcApplication;
import com.chengzhi.chengrpc.config.RegistryConfig;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.model.ServiceMetaInfo;
import com.chengzhi.chengrpc.registry.LocalRegistry;
import com.chengzhi.chengrpc.registry.Registry;
import com.chengzhi.chengrpc.registry.RegistryFactory;
import com.chengzhi.chengzhirpcspringbootstarter.annotation.RpcService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class RpcProviderBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if(rpcService != null){
            //需要注册服务
            //获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            //默认值处理
            if(interfaceClass == void.class){
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();
            //注册服务
            //本地注册
            LocalRegistry.registerService(serviceName,beanClass);

            //全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            //注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败",e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
