package com.chengzhi.chengrpc.server.tcp;

import com.chengzhi.chengrpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class VertxTcpServer implements HttpServer {


    @Override
    public void doStart(int port) {
        //创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        //创建 TCP 服务器
        NetServer server = vertx.createNetServer();

        //处理请求
        server.connectHandler(socket -> {

                    //构造parser
                    RecordParser recordParser = RecordParser.newFixed(13);
                    recordParser.setOutput(new Handler<Buffer>() {

                        //初始化
                        int size = -1;
                        // 一次完整的读取（头 + 体）
                        Buffer resultBuffer = Buffer.buffer();

                        @Override
                        public void handle(Buffer buffer) {
                            if(-1 == size){
                                size = buffer.getInt(4);
                                recordParser.fixedSizeMode(size);
                                resultBuffer.appendBuffer(buffer);
                            }else{
                                resultBuffer.appendBuffer(buffer);
                                System.out.println(resultBuffer.toString());
                                recordParser.fixedSizeMode(13);
                                size = -1;
                                resultBuffer = Buffer.buffer();
                            }
                        }
                    });
                    socket.handler(recordParser);
                });
        //启动 Tcp 服务器并监听指定端口
        server.listen(port,result -> {
           if(result.succeeded()){
               System.out.println("Server started on port " + port);
           } else{
               System.out.println("Failed to start TCP server " + result.cause());
           }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
