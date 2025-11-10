package com.chengzhi.chengrpc.bootstrap;

import com.chengzhi.chengrpc.RpcApplication;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class ConsumerBootstrap {

    /**
     * 初始化
     */
    public static void init(){
        //RPC 框架初始化（配置和注册中心）
        RpcApplication.init();
    }

}
