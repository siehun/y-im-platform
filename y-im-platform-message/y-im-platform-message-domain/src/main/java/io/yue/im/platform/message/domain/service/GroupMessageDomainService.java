package io.yue.im.platform.message.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.yue.im.platform.common.model.entity.GroupMessage;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.platform.message.domain.event.IMGroupMessageTxEvent;

import java.util.Date;
import java.util.List;

/**
 * @description 群聊消息
 */
public interface GroupMessageDomainService extends IService<GroupMessage> {

    /**
     * 保存群聊消息
     */
    boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent);

    /**
     * 检测某条消息是否存在
     */
    boolean checkExists(Long messageId);

    /**
     * 拉取未读消息
     */
    List<GroupMessageVO> getUnreadGroupMessageList(Long groupId, Date sendTime, Long sendId, Integer status, Long maxReadId, Integer limitCount);

    /**
     * 拉取全站消息
     */
    List<GroupMessageVO> loadGroupMessageList(Long minId, Date minDate, List<Long> ids, Integer status,  Integer limitCount);

    /**
     * 拉取在某个群的消息
     */
    List<GroupMessageVO> getHistoryMessage(Long groupId, Date sendTime, Integer status, long stIdx, long size);

    /**
     * 获取最大消息id
     */
    Long getMaxMessageId(Long groupId);

    /**
     * 查询指定的群聊消息
     */
    GroupMessageVO getGroupMessageById(Long messageId);

    /**
     * 更新群聊消息状态
     */
    int updateStatus(Integer status, Long messageId);
}
