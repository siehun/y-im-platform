package io.yue.im.platform.user.application.cache.service;

/**
 * @description 用户缓存接口
 */
public interface UserCacheService {

    /**
     * 更新缓存数据
     */
    void updateUserCache(Long userId);
}
