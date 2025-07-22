package io.yue.im.platform.message.domain.event;


import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.dto.GroupMessageDTO;

import java.util.Date;
import java.util.List;

/**
 * @description 群聊事务事件
 */
public class IMGroupMessageTxEvent extends IMMessageTxEvent {
    //消息发送人昵称
    private String sendNickName;
    //接收消息的用户列表
    private List<Long> userIds;
    //消息数据
    private GroupMessageDTO groupMessageDTO;

    public IMGroupMessageTxEvent() {
    }

    public IMGroupMessageTxEvent(Long id, Long senderId, String sendNickName, Integer terminal, Date sendTime, String destination, List<Long> userIds, GroupMessageDTO groupMessageDTO) {
        super(id, senderId, terminal, sendTime, destination, IMPlatformConstants.TYPE_MESSAGE_GROUP);
        this.sendNickName = sendNickName;
        this.groupMessageDTO = groupMessageDTO;
        this.userIds = userIds;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public GroupMessageDTO getGroupMessageDTO() {
        return groupMessageDTO;
    }

    public void setGroupMessageDTO(GroupMessageDTO groupMessageDTO) {
        this.groupMessageDTO = groupMessageDTO;
    }
}
