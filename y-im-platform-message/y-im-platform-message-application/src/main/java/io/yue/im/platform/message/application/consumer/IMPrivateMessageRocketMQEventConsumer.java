package io.yue.im.platform.message.application.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.model.IMPrivateMessage;
import io.yue.im.common.domain.model.IMUserInfo;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.enums.MessageStatus;
import io.yue.im.platform.common.model.vo.PrivateMessageVO;
import io.yue.im.platform.common.threadpool.PrivateMessageThreadPoolUtils;
import io.yue.im.platform.common.utils.BeanUtils;
import io.yue.im.platform.message.domain.event.IMPrivateMessageTxEvent;
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
 * @description 消费消息，接收单聊事务消息
 */
@Component
@ConditionalOnProperty(name = "message.mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE_GROUP, topic = IMPlatformConstants.TOPIC_PRIVATE_TX_MESSAGE)
public class IMPrivateMessageRocketMQEventConsumer implements RocketMQListener<String> {
    private final Logger logger = LoggerFactory.getLogger(IMPrivateMessageRocketMQEventConsumer.class);
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
            logger.info("rocketmq|privateMessageTxConsumer|接收消息微服务发送过来的单聊消息事件参数为空" );
            return;
        }
        logger.info("rocketmq|privateMessageTxConsumer|接收消息微服务发送过来的单聊消息事件|{}", message);
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = this.getEventMessage(message);
        if (imPrivateMessageTxEvent == null || imPrivateMessageTxEvent.getPrivateMessageDTO() == null){
            logger.error("rocketmq|privateMessageTxConsumer|接收消息微服务发送过来的单聊消息事件转换失败");
            return;
        }
        PrivateMessageVO privateMessageVO = BeanUtils.copyProperties(imPrivateMessageTxEvent.getPrivateMessageDTO(), PrivateMessageVO.class);
        //设置消息id
        privateMessageVO.setId(imPrivateMessageTxEvent.getId());
        //设置发送者id
        privateMessageVO.setSendId(imPrivateMessageTxEvent.getSenderId());
        //设置状态
        privateMessageVO.setStatus(MessageStatus.UNSEND.code());
        //发送时间
        privateMessageVO.setSendTime(imPrivateMessageTxEvent.getSendTime());
        //封装发送消息数据模型
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(privateMessageVO.getSendId(), imPrivateMessageTxEvent.getTerminal()));
        sendMessage.setReceiveId(privateMessageVO.getRecvId());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(privateMessageVO);
        imClient.sendPrivateMessage(sendMessage);
        logger.info("发送私聊消息，发送id:{},接收id:{}，内容:{}", privateMessageVO.getSendId(), privateMessageVO.getRecvId(), privateMessageVO.getContent());

//        // 向指定的用户发送消息，触发OpenAI大模型流程
//        if (IMPlatformConstants.OPENAI_USER_ID.equals(privateMessageVO.getRecvId())){
//            PrivateMessageThreadPoolUtils.execute(() -> {
//                // 发送大模型消息
//                this.sendOpenAiMessage(privateMessageVO, imPrivateMessageTxEvent.getTerminal());
//            });
//        }
    }

    /**
     * 发送OpenAi大模型消息
     */
//    private void sendOpenAiMessage(PrivateMessageVO privateMessageVO, Integer terminal){
//        // 不是对接OpenAI的账号
//        Long recvId = privateMessageVO.getRecvId();
//        if (!IMPlatformConstants.OPENAI_USER_ID.equals(recvId)){
//            return;
//        }
//        try {
//            logger.info("单聊消息发送OpenAI消息开始");
//            String openAIMessage = openAIDubboService.sendMessage(privateMessageVO.getContent());
//            privateMessageVO.setSendTime(new Date());
//            privateMessageVO.setContent(openAIMessage);
//            IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
//            sendMessage.setSender(new IMUserInfo(privateMessageVO.getRecvId(), terminal));
//            sendMessage.setReceiveId(privateMessageVO.getSendId());
//            sendMessage.setSendToSelf(true);
//            sendMessage.setData(privateMessageVO);
//            imClient.sendPrivateMessage(sendMessage);
//            logger.info("单聊消息发送OpenAI消息结束");
//        } catch (IOException e) {
//            logger.error("对接OpenAI大模型消息异常: ", e);
//        }
//    }

    private IMPrivateMessageTxEvent getEventMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMPrivateMessageTxEvent.class);
    }
}
