package io.yue.im.platform.message.application.service;


import io.yue.im.platform.common.model.dto.GroupMessageDTO;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.platform.message.domain.event.IMGroupMessageTxEvent;

import java.util.List;

/**
 * @description 群聊服务
 */
public interface GroupMessageService {

    /**
     * 发送群聊消息
     */
    Long sendMessage(GroupMessageDTO dto);
    /**
     * 保存群聊消息
     */
    boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent);

    /**
     * 检测某条消息是否存在
     */
    boolean checkExists(Long messageId);

    /**
     *
     * 异步拉取群聊消息，通过websocket异步推送
     */
    void pullUnreadMessage();

    /**
     * 拉取消息，只能拉取最近1个月的消息，一次拉取100条
     */
    List<GroupMessageVO> loadMessage(Long minId);

    /**
     * 拉取历史聊天记录
     */
    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);

    /**
     * 消息已读,同步其他终端，清空未读数量
     */
    void readedMessage(Long groupId);

    /**
     * 撤回消息
     */
    void recallMessage(Long id);
}

