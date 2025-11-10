package com.chengzhi.chengrpc;

import com.chengzhi.chengrpc.config.RegistryConfig;
import com.chengzhi.chengrpc.config.RpcConfig;
import com.chengzhi.chengrpc.constant.RpcConstant;
import com.chengzhi.chengrpc.registry.Registry;
import com.chengzhi.chengrpc.registry.RegistryFactory;
import com.chengzhi.chengrpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 徐晟智
 * @version 1.0
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，支持传入自定义配置
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}",newRpcConfig.toString());
        //注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init, config = {}",registryConfig);

        //创建并注册 Shutdown Hook， JVM 退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * 初始化
     */
    public static void init(){
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            //配置加载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null){
            synchronized (RpcApplication.class){
                if(rpcConfig == null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
