package io.yue.im.platform.group.application.cache;


import io.yue.im.platform.group.domain.event.IMGroupEvent;

/**
 * @description 群组缓存服务
 */
public interface GroupCacheService {

    /**
     * 更新群组缓存
     */
    void updateGroupCache(IMGroupEvent imGroupEvent);
}
