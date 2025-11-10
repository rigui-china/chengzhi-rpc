package com.chengzhi.chengrpc.fault.tolerant;

import com.chengzhi.chengrpc.spi.SpiLoader;

/**
 * @author 徐晟智
 * @version 1.0
 */

public class TolerantStrategyFactory {
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 默认容错策略
     */
    private static final TolerantStrategy DEFAULT_RETRY_STRATEGY = new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}
