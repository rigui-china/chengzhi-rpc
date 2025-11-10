package com.chengzhi.chengrpc.server;

/**
 * @author 徐晟智
 * @version 1.0
 */

public interface HttpServer {
    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}
