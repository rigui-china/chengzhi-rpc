package com.chengzhi.chengrpc.fault.tolerant;

import com.chengzhi.chengrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * 静默处理异常 - 容错策略
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("静默处理异常",e);
        return new RpcResponse();
    }
}
