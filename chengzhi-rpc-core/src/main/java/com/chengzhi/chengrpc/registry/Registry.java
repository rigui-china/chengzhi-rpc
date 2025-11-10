package com.chengzhi.chengrpc.registry;

import com.chengzhi.chengrpc.config.RegistryConfig;
import com.chengzhi.chengrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * @author 徐晟智
 * @version 1.0
 */

public interface Registry {
    /**
     * 初始化
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务（服务端）
     * @param serviceMetaInfo
     * @throws Exception
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务
     * @param serviceMetaInfo
     * @throws Exception
     */
    void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务发现（获取某个节点的所有服务，消费端）
     * @param serviceKey
     * @return
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 服务销毁
     */
    void destroy();

    /**
     * 心跳检测（服务端）
     */
    void heartBeat();

    /**
     * 监听（消费端）
     * @param serviceNodeKey
     */
    void watch(String serviceNodeKey);
}
