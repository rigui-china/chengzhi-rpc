package com.chengzhi.chengrpc.serializer;

import com.chengzhi.chengrpc.spi.SpiLoader;



/**
 * @author 徐晟智
 * @version 1.0
 */

public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }


    /**
     * 默认化序列器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     */

    public static Serializer getInstance(String key){
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
