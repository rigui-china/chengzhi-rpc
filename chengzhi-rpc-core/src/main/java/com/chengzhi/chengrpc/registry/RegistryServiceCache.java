package com.chengzhi.chengrpc.registry;

/**
 * @author 徐晟智
 * @version 1.0
 */

import com.chengzhi.chengrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心服务本地缓存
 */
public class RegistryServiceCache {

    /**
     * 服务缓存
     */
    List<ServiceMetaInfo> serviceCahce;

    /**
     * 写缓存
     * @param newServiceCahce
     */
    void writeCache(List<ServiceMetaInfo> newServiceCahce) {
        this.serviceCahce = newServiceCahce;
    }

    /**
     * 读缓存
     * @return
     */
    List<ServiceMetaInfo> readCache() {
        return this.serviceCahce;
    }

    /**
     * 清空缓存
     */
    void clearCache() {
        this.serviceCahce = null;
    }
}
