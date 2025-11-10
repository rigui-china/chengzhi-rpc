package com.chengzhi.chengrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.chengzhi.chengrpc.RpcApplication;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.constant.RpcConstant;
import com.chengzhi.chengrpc.fault.retry.RetryStrategy;
import com.chengzhi.chengrpc.fault.retry.RetryStrategyFactory;
import com.chengzhi.chengrpc.fault.tolerant.TolerantStrategy;
import com.chengzhi.chengrpc.fault.tolerant.TolerantStrategyFactory;
import com.chengzhi.chengrpc.loadbalancer.LoadBalancer;
import com.chengzhi.chengrpc.loadbalancer.LoadBalancerFactory;
import com.chengzhi.chengrpc.model.RpcRequest;
import com.chengzhi.chengrpc.model.RpcResponse;
import com.chengzhi.chengrpc.model.ServiceMetaInfo;
import com.chengzhi.chengrpc.protocol.*;
import com.chengzhi.chengrpc.registry.Registry;
import com.chengzhi.chengrpc.registry.RegistryFactory;
import com.chengzhi.chengrpc.serializer.JdkSerializer;
import com.chengzhi.chengrpc.serializer.Serializer;
import com.chengzhi.chengrpc.serializer.SerializerFactory;
import com.chengzhi.chengrpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //执行序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        String serviceName = method.getDeclaringClass().getName();
        //构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

            //从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            //负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            //将调用方法名（请求路径）作为负载均衡参数
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("methodName",rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

            //发送 Tcp 请求
            //使用重试机制
            RpcResponse rpcResponse;
            try{
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() ->
                        VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo));
            } catch (Exception e) {
                //容错机制
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
                rpcResponse = tolerantStrategy.doTolerant(null, e);
            }
            return rpcResponse.getData();

    }
}
