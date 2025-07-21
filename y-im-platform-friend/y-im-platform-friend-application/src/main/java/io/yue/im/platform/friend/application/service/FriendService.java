package io.yue.im.platform.friend.application.service;


import io.yue.im.platform.common.model.entity.Friend;
import io.yue.im.platform.common.model.vo.FriendVO;

import java.util.List;

/**
 * @description 好友应用层服务
 */
public interface FriendService {

    /**
     * 根据用户id获取好友的id列表
     */
    List<Long> getFriendIdList(Long userId);

    /**
     * 判断用户2是否用户1的好友
     *
     * @param userId1 用户1的id
     * @param userId2 用户2的id
     * @return true/false
     */
    Boolean isFriend(Long userId1, Long userId2);


    /**
     * 查询用户的所有好友
     * @param userId   用户id
     * @return 好友列表
     */
    List<FriendVO> findFriendByUserId(Long userId);

    /**
     * 查询用户的所有好友
     * @param userId   用户id
     * @return 好友列表
     */
    List<Friend> getFriendByUserId(Long userId);

    /**
     * 添加好友，互相建立好友关系
     *
     * @param friendId 好友的用户id
     */
    void addFriend(Long friendId);

    /**
     * 删除好友，双方都会解除好友关系
     *
     * @param friendId 好友的用户id
     */
    void delFriend(Long friendId);

    /**
     * 更新好友信息，主要是头像和昵称
     *
     * @param vo  好友vo
     */
    void update(FriendVO vo);

    /**
     * 查询指定的某个好友信息
     *
     * @param friendId 好友的用户id
     * @return 好友信息
     */
    FriendVO findFriend(Long friendId);
}
