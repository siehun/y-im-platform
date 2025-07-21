package io.yue.im.platform.user.application.event;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.user.application.cache.service.UserCacheService;
import io.yue.im.platform.user.domain.event.IMUserEvent;
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
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.EVENT_USER_CONSUMER_GROUP, topic = IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_USER)
public class IMUserRocketMQEventHandler implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMUserRocketMQEventHandler.class);

    @Autowired
    private UserCacheService userCacheService;

    @Override
    public void onMessage(String message) {
        logger.info("rocketmq|userEvent|接收用户事件|{}", message);
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|userEvent|接收用户事件参数错误" );
            return;
        }
        IMUserEvent userEvent = this.getEventMessage(message);
        userCacheService.updateUserCache(userEvent.getId());
    }

    private IMUserEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMUserEvent.class);
    }
}
