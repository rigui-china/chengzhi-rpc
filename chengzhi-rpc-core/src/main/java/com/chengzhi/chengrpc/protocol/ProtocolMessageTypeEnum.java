package com.chengzhi.chengrpc.protocol;

import lombok.Getter;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * 协议消息的类型枚举
 */
@Getter
public enum ProtocolMessageTypeEnum {
    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3)
    ;

    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    public static ProtocolMessageTypeEnum getEnumByKey(int key) {
        for (ProtocolMessageTypeEnum e : ProtocolMessageTypeEnum.values()) {
            if(e.key == key) {
                return e;
            }
        }
        return null;
    }
}
