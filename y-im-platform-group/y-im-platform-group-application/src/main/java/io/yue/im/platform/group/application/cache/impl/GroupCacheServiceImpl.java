package io.yue.im.platform.group.application.cache.impl;

import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.params.GroupParams;
import io.yue.im.platform.group.application.cache.GroupCacheService;
import io.yue.im.platform.group.domain.event.IMGroupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description 缓存更新
 */
@Service
public class GroupCacheServiceImpl implements GroupCacheService {
    private final Logger logger = LoggerFactory.getLogger(GroupCacheServiceImpl.class);
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Override
    public void updateGroupCache(IMGroupEvent imGroupEvent) {
        if (imGroupEvent == null){
            return;
        }
        switch (imGroupEvent.getHandler()){
            case IMPlatformConstants.GROUP_HANDLER_CREATE:
                this.handlerCreate(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_MODIFY:
                this.handlerModify(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_DELETE:
                this.handlerDelete(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_QUIT:
                this.handlerQuit(imGroupEvent);
                break;
            case IMPlatformConstants.GROUP_HANDLER_KICK:
                this.handlerKick(imGroupEvent);
            case IMPlatformConstants.GROUP_HANDLER_INVITE:
                this.handlerInvite(imGroupEvent);
            default:
                logger.info("groupCacheService|群组缓存服务接收到的事件参数为|{}", JSONObject.toJSONString(imGroupEvent));
        }
    }

    private void handlerInvite(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入邀请人事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        this.handlerGroupMember(imGroupEvent);
    }

    private void handlerKick(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入踢人事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        this.handlerGroupMember(imGroupEvent);
    }

    private void handlerQuit(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入退群事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        this.handlerGroupMember(imGroupEvent);
    }

    private void handlerDelete(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入解散群事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        String redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_VO_SINGLE_KEY, new GroupParams(imGroupEvent.getUserId(), imGroupEvent.getId()));
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_LIST_KEY, imGroupEvent.getUserId());
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_SINGLE_KEY, imGroupEvent.getId());
        distributedCacheService.delete(redisKey);
        //群成员自动过期即可
    }

    private void handlerModify(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入修改群事件处理|{}", JSONObject.toJSONString(imGroupEvent));
        String redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_VO_SINGLE_KEY, new GroupParams(imGroupEvent.getUserId(), imGroupEvent.getId()));
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_SINGLE_KEY, imGroupEvent.getId());
        distributedCacheService.delete(redisKey);
    }

    private void handlerCreate(IMGroupEvent imGroupEvent) {
        logger.info("groupCacheService|进入保存群组事件处理|{}", JSONObject.toJSONString(imGroupEvent));
    }

    private void handlerGroupMember(IMGroupEvent imGroupEvent){
        String redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_LIST_KEY, imGroupEvent.getId());
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_SIMPLE_KEY, new GroupParams(imGroupEvent.getUserId(), imGroupEvent.getId()));
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_ID_KEY, imGroupEvent.getId());
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_MEMBER_LIST_SIMPLE_KEY, imGroupEvent.getUserId());
        distributedCacheService.delete(redisKey);

        redisKey = distributedCacheService.getKey(IMPlatformConstants.PLATFORM_REDIS_GROUP_LIST_KEY, imGroupEvent.getUserId());
        distributedCacheService.delete(redisKey);
    }
}
