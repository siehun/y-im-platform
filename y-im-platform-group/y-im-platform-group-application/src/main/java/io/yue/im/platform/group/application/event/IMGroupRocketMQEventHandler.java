package io.yue.im.platform.group.application.event;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.group.application.cache.GroupCacheService;
import io.yue.im.platform.group.domain.event.IMGroupEvent;
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
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.EVENT_GROUP_CONSUMER_GROUP, topic = IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_GROUP)
public class IMGroupRocketMQEventHandler implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMGroupRocketMQEventHandler.class);

    @Autowired
    private GroupCacheService groupCacheService;

    @Override
    public void onMessage(String message) {
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|groupEvent|接收群组事件参数错误" );
            return;
        }
        logger.info("rocketmq|groupEvent|接收群组事件参数|{}", message);
        IMGroupEvent groupEvent = this.getEventMessage(message);
        groupCacheService.updateGroupCache(groupEvent);
    }

    private IMGroupEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMGroupEvent.class);
    }
}
