package io.yue.im.platform.dubbo.friend;


import io.yue.im.platform.common.model.entity.Friend;

import java.util.List;

/**
 * @description 用户Dubbo服务
 */
public interface FriendDubboService {

    /**
     * 判断用户2是否用户1的好友
     *
     * @param userId1 用户1的id
     * @param userId2 用户2的id
     * @return true/false
     */
    Boolean isFriend(Long userId1, Long userId2);

    /**
     * 根据用户id获取好友的id列表
     */
    List<Long> getFriendIdList(Long userId);

    /**
     * 根据用户id获取好友列表
     */
    List<Friend> getFriendByUserId(Long userId);
}
