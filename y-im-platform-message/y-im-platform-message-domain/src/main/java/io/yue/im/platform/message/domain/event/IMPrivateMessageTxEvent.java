package io.yue.im.platform.message.domain.event;


import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.dto.PrivateMessageDTO;

import java.util.Date;

/**
 * @description 单聊事务事件
 */
public class IMPrivateMessageTxEvent extends IMMessageTxEvent {
    //消息数据
    private PrivateMessageDTO privateMessageDTO;

    public IMPrivateMessageTxEvent() {
    }

    public IMPrivateMessageTxEvent(Long id, Long senderId, Integer terminal, String destination, Date sendTime, PrivateMessageDTO privateMessageDTO) {
        super(id, senderId, terminal, sendTime, destination, IMPlatformConstants.TYPE_MESSAGE_PRIVATE);
        this.privateMessageDTO = privateMessageDTO;
    }

    public PrivateMessageDTO getPrivateMessageDTO() {
        return privateMessageDTO;
    }

    public void setPrivateMessageDTO(PrivateMessageDTO privateMessageDTO) {
        this.privateMessageDTO = privateMessageDTO;
    }
}
