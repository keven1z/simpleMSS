package com.keven1z.core.utils;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * transformer守护者
 * <p>
 * 用来保护transformer的操作所产生的事件不被响应
 * </p>
 */
public class TransformerProtector {


    private final ThreadLocal<AtomicInteger> isInProtectingThreadLocal = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    /**
     * 进入守护区域
     *
     * @return 守护区域当前引用计数
     */
    public int enterProtecting() {
        final int referenceCount = isInProtectingThreadLocal.get().getAndIncrement();
        return referenceCount;
    }

    /**
     * 离开守护区域
     *
     * @return 守护区域当前引用计数
     */
    public int exitProtecting() {
        final int referenceCount = isInProtectingThreadLocal.get().decrementAndGet();
        // assert referenceCount >= 0;
        if (referenceCount == 0) {
            isInProtectingThreadLocal.remove();

        } else if (referenceCount > 0) {

        } else {
        }
        return referenceCount;
    }

    /**
     * 判断当前是否处于守护区域中
     *
     * @return TRUE:在守护区域中；FALSE：非守护区域中
     */
    public boolean isInProtecting() {
        final boolean res = isInProtectingThreadLocal.get().get() > 0;
        if (!res) {
            isInProtectingThreadLocal.remove();
        }
        return res;
    }

    /**
     * Sandbox守护者单例
     */
    public static final TransformerProtector instance = new TransformerProtector();

}
