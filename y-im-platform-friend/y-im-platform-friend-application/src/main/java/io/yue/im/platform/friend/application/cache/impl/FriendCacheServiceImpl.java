package io.yue.im.platform.friend.application.cache.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.cache.lock.DistributedLock;
import io.yue.im.common.cache.lock.factory.DistributedLockFactory;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.entity.Friend;
import io.yue.im.platform.common.model.vo.FriendVO;
import io.yue.im.platform.friend.application.cache.FriendCacheService;
import io.yue.im.platform.friend.domain.event.IMFriendEvent;
import io.yue.im.platform.friend.domain.model.command.FriendCommand;
import io.yue.im.platform.friend.domain.service.FriendDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description
 */
@Service
public class FriendCacheServiceImpl implements FriendCacheService {
    private final Logger logger = LoggerFactory.getLogger(FriendCacheServiceImpl.class);
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private FriendDomainService domainService;
    @Autowired
    private DistributedLockFactory distributedLockFactory;

    @Override
    public void updateFriendCache(IMFriendEvent friendEvent) {
        if (friendEvent == null || friendEvent.getId() == null){
            logger.info("IMFriendCacheService|更新分布式缓存时，参数为空");
            return;
        }
        //获取分布式锁，保证只有一个线程在更新分布式缓存
        DistributedLock lock = distributedLockFactory.getDistributedLock(IMPlatformConstants.IM_FRIEND_UPDATE_CACHE_LOCK_KEY.concat(String.valueOf(friendEvent.getId())));
        try{
            boolean isSuccess = lock.tryLock();
            if (!isSuccess){
                return;
            }
            switch (friendEvent.getHandler()){
                //添加好友
                case IMPlatformConstants.FRIEND_HANDLER_BIND:
                    this.bindFrind(friendEvent);
                    break;
                //删除好友
                case IMPlatformConstants.FRIEND_HANDLER_UNBIND:
                    this.unbindFriend(friendEvent);
                    break;
                //更新好友信息
                case IMPlatformConstants.FRIEND_HANDLER_UPDATE:
                    this.updateFriend(friendEvent);
                    break;
                default:
                    this.updateFriend(friendEvent);

            }
        }catch (Exception e){
            logger.error("IMUserCache|更新分布式缓存失败|{}", JSONObject.toJSONString(friendEvent));
        }finally {
            lock.unlock();
        }
    }

    private void updateFriend(IMFriendEvent friendEvent) {
        String redisKey = "";
        //获取好友列表
        List<Friend> friendList = domainService.getFriendByUserId(friendEvent.getId());
        if (!CollectionUtil.isEmpty(friendList)){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, friendEvent.getId());
            distributedCacheService.set(redisKey, friendList, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }
        FriendCommand friendCommand = new FriendCommand(friendEvent.getId(), friendEvent.getFriendId());
        FriendVO friendVO = domainService.findFriend(friendCommand);
        if (friendVO != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, friendCommand);
            distributedCacheService.set(redisKey, friendVO, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }
    }

    private void unbindFriend(IMFriendEvent friendEvent) {
        String redisKey = "";
        if (friendEvent.getFriendId() != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY, friendEvent.getId());
            distributedCacheService.removeSet(redisKey, String.valueOf(friendEvent.getFriendId()));
        }
        //获取好友列表
        List<Friend> friendList = domainService.getFriendByUserId(friendEvent.getId());
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, friendEvent.getId());
        if (!CollectionUtil.isEmpty(friendList)){
            distributedCacheService.set(redisKey, friendList, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }else{
            distributedCacheService.delete(redisKey);
        }
        FriendCommand friendCommand = new FriendCommand(friendEvent.getId(), friendEvent.getFriendId());
        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, friendCommand);
        distributedCacheService.delete(redisKey);
    }

    private void bindFrind(IMFriendEvent friendEvent) {
        String redisKey = "";
        if (friendEvent.getFriendId() != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SET_KEY, friendEvent.getId());
            distributedCacheService.addSet(redisKey, String.valueOf(friendEvent.getFriendId()));
        }
        //获取好友列表
        List<Friend> friendList = domainService.getFriendByUserId(friendEvent.getId());
        if (!CollectionUtil.isEmpty(friendList)){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_LIST_KEY, friendEvent.getId());
            distributedCacheService.set(redisKey, friendList, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }
        FriendCommand friendCommand = new FriendCommand(friendEvent.getId(), friendEvent.getFriendId());
        FriendVO friendVO = domainService.findFriend(friendCommand);
        if (friendVO != null){
            redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_FRIEND_SINGLE_KEY, friendCommand);
            distributedCacheService.set(redisKey, friendVO, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }
    }
}
