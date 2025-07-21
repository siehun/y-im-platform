package io.yue.im.platform.friend.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.entity.Friend;
import io.yue.im.platform.common.model.entity.User;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.vo.FriendVO;
import io.yue.im.platform.common.session.SessionContext;
import io.yue.im.platform.dubbo.user.UserDubboService;
import io.yue.im.platform.friend.application.service.FriendService;
import io.yue.im.platform.friend.domain.model.command.FriendCommand;
import io.yue.im.platform.friend.domain.service.FriendDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description 好友应用层服务实现
 */
@Service
public class FriendServiceImpl implements FriendService {
    private final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private FriendDomainService domainService;
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private UserDubboService userDubboService;

    @Override
    public List<Long> getFriendIdList(Long userId) {
        if (userId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        List<Friend> friendList = this.getFriendByUserId(userId);
        if (CollectionUtil.isEmpty(friendList)){
            return Collections.emptyList();
        }
        return friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
    }

    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        String redisKey = IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY.concat(String.valueOf(userId1));
        Boolean result = distributedCacheService.isMemberSet(redisKey, String.valueOf(userId2));
        if (BooleanUtil.isTrue(result)){
            return result;
        }
        result = domainService.isFriend(userId1, userId2);
        if (BooleanUtil.isTrue(result)){
            distributedCacheService.addSet(redisKey, String.valueOf(userId2));
        }
        return result;
    }

    @Override
    public List<FriendVO> findFriendByUserId(Long userId) {
        if (userId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        List<Friend> friendList = this.getFriendByUserId(userId);
        if (CollectionUtil.isEmpty(friendList)){
            return Collections.emptyList();
        }
        return friendList.stream().map(friend -> new FriendVO(friend.getFriendId(), friend.getFriendNickName(), friend.getFriendHeadImage())).collect(Collectors.toList());
    }

    @Override
    public List<Friend> getFriendByUserId(Long userId) {
        if (userId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThroughList(
                IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, userId,
                Friend.class,
                domainService::getFriendByUserId,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFriend(Long friendId) {
        if (friendId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        Long userId = SessionContext.getSession().getUserId();
        if(Objects.equals(userId, friendId)){
            throw new IMException(HttpCode.PROGRAM_ERROR, "不允许添加自己为好友");
        }
        FriendCommand friendCommand = new FriendCommand(userId, friendId);
        User user = userDubboService.getUserById(friendId);
        domainService.bindFriend(friendCommand, user == null ? "" : user.getHeadImage(), user == null ? "" : user.getNickName());

        friendCommand = new FriendCommand(friendId, userId);
        user = userDubboService.getUserById(userId);
        domainService.bindFriend(friendCommand, user == null ? "" : user.getHeadImage(), user == null ? "" : user.getNickName());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delFriend(Long friendId) {
        if (friendId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        Long userId = SessionContext.getSession().getUserId();
        FriendCommand friendCommand = new FriendCommand(userId, friendId);
        domainService.unbindFriend(friendCommand);

        friendCommand = new FriendCommand(friendId, userId);
        domainService.unbindFriend(friendCommand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FriendVO vo) {
        if (vo == null || vo.getId() == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        domainService.update(vo, SessionContext.getSession().getUserId());
    }

    @Override
    public FriendVO findFriend(Long friendId) {
        if (friendId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY,
                new FriendCommand(SessionContext.getSession().getUserId(), friendId),
                FriendVO.class,
                domainService::findFriend,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }
}
