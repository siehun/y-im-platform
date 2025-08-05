package io.yue.im.platform.message.application.tx;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.message.application.service.GroupMessageService;
import io.yue.im.platform.message.application.service.PrivateMessageService;
import io.yue.im.platform.message.domain.event.IMGroupMessageTxEvent;
import io.yue.im.platform.message.domain.event.IMMessageTxEvent;
import io.yue.im.platform.message.domain.event.IMPrivateMessageTxEvent;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @description 监听事务消息
 */
@Component
@RocketMQTransactionListener(rocketMQTemplateBeanName = "rocketMQTemplate")
public class IMMessageTxListener implements RocketMQLocalTransactionListener {
    private final Logger logger = LoggerFactory.getLogger(IMMessageTxListener.class);
    @Autowired
    private PrivateMessageService privateMessageService;
    @Autowired
    private GroupMessageService groupMessageService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try{
            IMMessageTxEvent imMessageTxEvent = this.getTxMessage(message);
            switch (imMessageTxEvent.getMessageType()){
                //单聊
                case IMPlatformConstants.TYPE_MESSAGE_PRIVATE:
                    return executePrivateMessageLocalTransaction(message);
                //群聊
                case IMPlatformConstants.TYPE_MESSAGE_GROUP:
                    return executeGroupMessageLocalTransaction(message);
                default:
                    return executePrivateMessageLocalTransaction(message);
            }
        }catch (Exception e){
            logger.info("executeLocalTransaction|消息微服务提交本地事务异常|{}", e.getMessage(), e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    private RocketMQLocalTransactionState executeGroupMessageLocalTransaction(Message message) {
        IMGroupMessageTxEvent imGroupMessageTxEvent = this.getTxGroupMessage(message);
        boolean result = groupMessageService.saveIMGroupMessageTxEvent(imGroupMessageTxEvent);
        if (result){
            logger.info("executeGroupMessageLocalTransaction|消息微服务提交群聊本地事务成功|{}", imGroupMessageTxEvent.getId());
            return RocketMQLocalTransactionState.COMMIT;
        }
        logger.info("executeGroupMessageLocalTransaction|消息微服务提交群聊本地事务失败|{}", imGroupMessageTxEvent.getId());
        return RocketMQLocalTransactionState.ROLLBACK;
    }

    private RocketMQLocalTransactionState executePrivateMessageLocalTransaction(Message message) {
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = this.getTxPrivateMessage(message);
        boolean result = privateMessageService.saveIMPrivateMessageSaveEvent(imPrivateMessageTxEvent);
        if (result){
            logger.info("executePrivateMessageLocalTransaction|消息微服务提交单聊本地事务成功|{}", imPrivateMessageTxEvent.getId());
            return RocketMQLocalTransactionState.COMMIT;
        }
        logger.info("executePrivateMessageLocalTransaction|消息微服务提交单聊本地事务失败|{}", imPrivateMessageTxEvent.getId());
        return RocketMQLocalTransactionState.ROLLBACK;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        IMMessageTxEvent imMessageTxEvent = this.getTxMessage(message);
        logger.info("checkLocalTransaction|消息微服务查询本地事务|{}", imMessageTxEvent.getId());
        Boolean submitTransaction = Boolean.FALSE;
        switch (imMessageTxEvent.getMessageType()){
            //单聊
            case IMPlatformConstants.TYPE_MESSAGE_PRIVATE:
                submitTransaction = privateMessageService.checkExists(imMessageTxEvent.getId());
                break;
            //群聊
            case IMPlatformConstants.TYPE_MESSAGE_GROUP:
                submitTransaction = groupMessageService.checkExists(imMessageTxEvent.getId());
                break;
            default:
                submitTransaction = privateMessageService.checkExists(imMessageTxEvent.getId());

        }
        return BooleanUtil.isTrue(submitTransaction) ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.UNKNOWN ;
    }

    private IMMessageTxEvent getTxMessage(Message msg){
        String messageString = new String((byte[]) msg.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(txStr, IMMessageTxEvent.class);
    }

    private IMPrivateMessageTxEvent getTxPrivateMessage(Message msg){
        String messageString = new String((byte[]) msg.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(txStr, IMPrivateMessageTxEvent.class);
    }
    private IMGroupMessageTxEvent getTxGroupMessage(Message msg){
        String messageString = new String((byte[]) msg.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(txStr, IMGroupMessageTxEvent.class);
    }
}
