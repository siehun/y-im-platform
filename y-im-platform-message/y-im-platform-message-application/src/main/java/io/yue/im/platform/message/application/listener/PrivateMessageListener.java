package io.yue.im.platform.message.application.listener;

import com.alibaba.fastjson.JSON;
import io.yue.im.common.domain.model.IMSendResult;
import io.yue.im.platform.common.model.vo.PrivateMessageVO;
import io.yue.im.sdk.domain.annotation.IMListener;
import io.yue.im.sdk.domain.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.yue.im.common.domain.enums.IMListenerType.*;

/**
 * @description 监听私聊消息
 */
@IMListener(listenerType = PRIVATE_MESSAGE)
public class PrivateMessageListener implements MessageListener<PrivateMessageVO> {
    private final Logger logger = LoggerFactory.getLogger(PrivateMessageListener.class);
    @Override
    public void doProcess(IMSendResult<PrivateMessageVO> result) {
        logger.info("PrivateMessageListener|监听到单聊消息数据|{}", JSON.toJSONString(result));
    }
}
