package com.chengzhi.chengrpc.server;

import io.vertx.core.Vertx;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class VertxHttpServer implements HttpServer{
    public void doStart(int port) {
        //创建 vertx 实例
        Vertx vertx = Vertx.vertx();
        //创建 HTTP 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //监听端口并处理请求
        server.requestHandler(new HttpServerHandler());

        //启动 HTTP 服务器并监听执行端口
        server.listen(port,result -> {
            if (result.succeeded()) {
                System.out.println("Server is listening on port " + port);
            }else{
                System.err.println("Failed to start Server " + result.cause());
            }
        });
    }
}
