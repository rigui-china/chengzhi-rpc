package com.chengzhi.chengrpc.serializer;

import java.io.IOException;

/**
 * @author 徐晟智
 * @version 1.0
 */

public interface Serializer {

    /**
     * 序列化
     * @param object
     * @return
     * @param <T>
     * @throws Exception
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws IOException;
}
