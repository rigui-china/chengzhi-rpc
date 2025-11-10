package com.chengzhi.chengrpc.server.tcp;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.chengzhi.chengrpc.model.RpcRequest;
import com.chengzhi.chengrpc.model.RpcResponse;
import com.chengzhi.chengrpc.protocol.ProtocolMessage;
import com.chengzhi.chengrpc.protocol.ProtocolMessageDecoder;
import com.chengzhi.chengrpc.protocol.ProtocolMessageEncoder;
import com.chengzhi.chengrpc.protocol.ProtocolMessageTypeEnum;
import com.chengzhi.chengrpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.lang.reflect.Method;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        //处理连接
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            //接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try{
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (Exception e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            //处理请求
            //构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try{
                //获取要调用的服务实现类，通过反射调用类
                Class<?> implClass = LocalRegistry.getService(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                //封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            //发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
                netSocket.write(encode);
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }

        });
        netSocket.handler(bufferHandlerWrapper);
    }
}
