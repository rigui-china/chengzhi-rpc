package com.chengzhi.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.chengzhi.chengrpc.model.RpcRequest;
import com.chengzhi.chengrpc.model.RpcResponse;
import com.chengzhi.chengrpc.serializer.JdkSerializer;
import com.chengzhi.chengrpc.serializer.Serializer;
import com.chengzhi.example.common.model.User;
import com.chengzhi.example.common.service.UserService;

import java.io.IOException;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        //指定序列化器
        Serializer serializer = new JdkSerializer();

        //发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try{
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            byte[] result;

            try(HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                        .body(bodyBytes)
                        .execute()){
                result = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
