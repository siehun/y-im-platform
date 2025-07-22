package io.yue.im.platform.message.application.service;


import io.yue.im.platform.common.model.dto.PrivateMessageDTO;
import io.yue.im.platform.common.model.vo.PrivateMessageVO;
import io.yue.im.platform.message.domain.event.IMPrivateMessageTxEvent;

import java.util.List;

/**
 * @description 单聊消息
 */
public interface PrivateMessageService {
    /**
     * 发送私聊消息
     */
    Long sendMessage(PrivateMessageDTO dto);

    /**
     * 保存单聊消息
     */
    boolean saveIMPrivateMessageSaveEvent(IMPrivateMessageTxEvent privateMessageSaveEvent);

    /**
     * 检测数据
     */
    boolean checkExists(Long messageId);

    /**
     * 异步拉取单聊未读消息
     */
    void pullUnreadMessage();

    /**
     * 拉取消息，只能拉取最近1个月的消息，一次拉取100条
     */
    List<PrivateMessageVO> loadMessage(Long minId);

    /**
     * 拉取历史聊天记录
     */
    List<PrivateMessageVO> getHistoryMessage(Long friendId, Long page, Long size);

    /**
     * 消息已读,将整个会话的消息都置为已读状态
     */
    void readedMessage(Long friendId);

    /**
     * 撤回消息
     */
    void recallMessage(Long id);

    /**
     *  获取某个会话中已读消息的最大id
     * @param friendId 好友id
     */
    Long getMaxReadedId(Long friendId);
}
