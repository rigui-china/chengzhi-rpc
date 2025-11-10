package com.chengzhi.chengrpc.loadbalancer;

import com.chengzhi.chengrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author 徐晟智
 * @version 1.0
 */

public interface LoadBalancer {

    /**
     * 选择服务调用
     * @param requestParams
     * @param serviceMetaInfoList
     * @return
     */
    ServiceMetaInfo select(Map<String,Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
