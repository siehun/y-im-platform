package io.yue.im.platform.common.risk.window;

/**
 * @description 滑动窗口服务
 */
public interface SlidingWindowLimitService {

    /**
     * 是否能通过滑动窗口的验证
     * @param key 事件标识
     * @param windowPeriod 窗口限流的周期，单位是毫秒
     * @param windowSize 滑动窗口大小
     * @return 是否通过
     */
    boolean passThough(String key, long windowPeriod, int windowSize);
}
