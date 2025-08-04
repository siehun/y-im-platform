package io.yue.im.platform.friend.application.event;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.friend.application.cache.FriendCacheService;
import io.yue.im.platform.friend.domain.event.IMFriendEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @description 基于RocketMQ的用户事件处理器
 */
@Component
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.EVENT_FRIEND_CONSUMER_GROUP, topic = IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_FRIEND)
public class IMFriendRocketMQEventHandler implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMFriendRocketMQEventHandler.class);

    @Autowired
    private FriendCacheService friendCacheService;

    @Override
    public void onMessage(String message) {
        logger.info("rocketmq|friendEvent|接收好友事件|{}", message);
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|friendEvent|接收好友事件参数错误" );
            return;
        }
        IMFriendEvent friendEvent = this.getEventMessage(message);
        friendCacheService.updateFriendCache(friendEvent);
    }

    private IMFriendEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMFriendEvent.class);
    }
}
