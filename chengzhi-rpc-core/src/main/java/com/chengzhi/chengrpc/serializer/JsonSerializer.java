package com.chengzhi.chengrpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chengzhi.chengrpc.model.RpcRequest;
import com.chengzhi.chengrpc.model.RpcResponse;

import java.io.IOException;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class JsonSerializer implements Serializer{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes,type);
        if(obj instanceof RpcRequest){
            return handleRequest((RpcRequest) obj,type);
        }
        if(obj instanceof RpcResponse){
            return handleResponse((RpcResponse) obj,type);
        }
        return obj;
    }
    private <T> T handleRequest(RpcRequest request,Class<T> type) throws IOException {
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] args = request.getArgs();
        //循环处理每个参数的类型
        for(int i = 0; i < parameterTypes.length; i++){
            Class<?> clazz = parameterTypes[i];
            //如果类型不同，则重新处理一下类型
            if(!clazz.isAssignableFrom(args[i].getClass())){
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes,clazz);
            }
        }
        return type.cast(request);
    }
    private <T> T handleResponse(RpcResponse response,Class<T> type) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(response.getData());
        response.setData(OBJECT_MAPPER.readValue(bytes,response.getDataType()));
        return type.cast(response);
    }
}
