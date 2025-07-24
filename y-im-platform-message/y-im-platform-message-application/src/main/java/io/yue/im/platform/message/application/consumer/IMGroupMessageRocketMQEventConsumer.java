package io.yue.im.platform.message.application.consumer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.cache.id.SnowFlakeFactory;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.model.IMGroupMessage;
import io.yue.im.common.domain.model.IMUserInfo;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.enums.MessageStatus;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.platform.common.threadpool.GroupMessageThreadPoolUtils;
import io.yue.im.platform.common.utils.BeanUtils;
import io.yue.im.platform.message.domain.event.IMGroupMessageTxEvent;
import io.yue.im.sdk.client.IMClient;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @description 消费消息，接收群聊事务消息
 */
@Component
@ConditionalOnProperty(name = "message.mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.TOPIC_GROUP_TX_MESSAGE_GROUP, topic = IMPlatformConstants.TOPIC_GROUP_TX_MESSAGE)
public class IMGroupMessageRocketMQEventConsumer implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMGroupMessageRocketMQEventConsumer.class);
    @Autowired
    private IMClient imClient;

    @Value("${bh.im.openai.userid:10000000001}")
    private Long openAiUserId;

    @Value("${bh.im.openai.username:binghe}")
    private String openAiUserName;

//    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
//    private OpenAIDubboService openAIDubboService;
    @Override
    public void onMessage(String message) {
        if (StrUtil.isEmpty(message)){
            logger.info("rocketmq|groupMessageTxConsumer|接收消息微服务发送过来的群聊消息事件参数为空" );
            return;
        }
        logger.info("rocketmq|groupMessageTxConsumer|接收消息微服务发送过来的群聊消息事件|{}", message);
        IMGroupMessageTxEvent imGroupMessageTxEvent = this.getEventMessage(message);
        if (imGroupMessageTxEvent == null || imGroupMessageTxEvent.getGroupMessageDTO() == null){
            logger.error("rocketmq|groupMessageTxConsumer|接收消息微服务发送过来的群聊消息事件转换失败");
            return;
        }
        GroupMessageVO groupMessageVO = BeanUtils.copyProperties(imGroupMessageTxEvent.getGroupMessageDTO(), GroupMessageVO.class);
        groupMessageVO.setId(imGroupMessageTxEvent.getId());
        groupMessageVO.setSendId(imGroupMessageTxEvent.getSenderId());
        groupMessageVO.setSendNickName(imGroupMessageTxEvent.getSendNickName());
        groupMessageVO.setSendTime(imGroupMessageTxEvent.getSendTime());
        groupMessageVO.setStatus(MessageStatus.UNSEND.code());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(imGroupMessageTxEvent.getSenderId(), imGroupMessageTxEvent.getTerminal()));
        sendMessage.setReceiveIds(imGroupMessageTxEvent.getUserIds());
        sendMessage.setData(groupMessageVO);
        imClient.sendGroupMessage(sendMessage);
        logger.info("发送群聊消息，发送者id:{},群组id:{},内容:{}", imGroupMessageTxEvent.getSenderId(), imGroupMessageTxEvent.getGroupMessageDTO().getGroupId(), imGroupMessageTxEvent.getGroupMessageDTO().getContent());
//        if (!CollectionUtil.isEmpty(groupMessageVO.getAtUserIds())
//                && groupMessageVO.getAtUserIds().size() == 1
//                && groupMessageVO.getAtUserIds().contains(openAiUserId)){
//            GroupMessageThreadPoolUtils.execute(() -> {
//                this.sendOpenAiMessage(groupMessageVO, imGroupMessageTxEvent);
//            });
//        }
    }

//    private void sendOpenAiMessage(GroupMessageVO groupMessageVO, IMGroupMessageTxEvent imGroupMessageTxEvent){
//        // 没有@用户，或者@用户列表中没有指定的用户，直接return, double check， 只能@置顶的一个用户
//        if (CollectionUtil.isEmpty(groupMessageVO.getAtUserIds())
//                || !groupMessageVO.getAtUserIds().contains(openAiUserId)
//                || groupMessageVO.getAtUserIds().size() > 1){
//            return;
//        }
//        try {
//            logger.info("群聊消息发送OpenAI消息开始");
//            String openAIMessage = openAIDubboService.sendMessage(groupMessageVO.getContent());
//            groupMessageVO.setSendId(openAiUserId);
//            groupMessageVO.setSendNickName(openAiUserName);
//            groupMessageVO.setSendTime(new Date());
//            groupMessageVO.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
//            groupMessageVO.setContent(openAIMessage);
//            IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
//            sendMessage.setSender(new IMUserInfo(openAiUserId, imGroupMessageTxEvent.getTerminal()));
//            sendMessage.setReceiveIds(imGroupMessageTxEvent.getUserIds());
//            sendMessage.setData(groupMessageVO);
//            imClient.sendGroupMessage(sendMessage);
//            logger.info("群聊消息发送OpenAI消息结束");
//        } catch (IOException e) {
//            logger.error("对接OpenAI大模型消息异常: ", e);
//        }
//    }

    private IMGroupMessageTxEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMGroupMessageTxEvent.class);
    }
}
