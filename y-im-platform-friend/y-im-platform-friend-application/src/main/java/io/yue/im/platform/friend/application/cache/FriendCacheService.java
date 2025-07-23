package io.yue.im.platform.friend.application.cache;


import io.yue.im.platform.friend.domain.event.IMFriendEvent;

/**
 * @description 好友缓存服务
 */
public interface FriendCacheService {

    /**
     * 更新好友缓存
     */
    void updateFriendCache(IMFriendEvent friendEvent);
}
