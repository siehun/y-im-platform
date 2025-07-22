package io.yue.im.platform.message.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.entity.GroupMessage;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.enums.MessageStatus;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.platform.common.utils.BeanUtils;
import io.yue.im.platform.message.domain.event.IMGroupMessageTxEvent;
import io.yue.im.platform.message.domain.repository.GroupMessageRepository;
import io.yue.im.platform.message.domain.service.GroupMessageDomainService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @description 群聊消息
 */
@Service
public class GroupMessageDomainServiceImpl extends ServiceImpl<GroupMessageRepository, GroupMessage> implements GroupMessageDomainService {
    @Override
    public boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent) {
        if (imGroupMessageTxEvent == null || imGroupMessageTxEvent.getGroupMessageDTO() == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        GroupMessage groupMessage = BeanUtils.copyProperties(imGroupMessageTxEvent, GroupMessage.class);
        if (groupMessage == null){
            throw new IMException(HttpCode.PROGRAM_ERROR, "转换群聊消息失败");
        }
        groupMessage.setId(imGroupMessageTxEvent.getId());
        groupMessage.setGroupId(imGroupMessageTxEvent.getGroupMessageDTO().getGroupId());
        groupMessage.setSendId(imGroupMessageTxEvent.getSenderId());
        groupMessage.setSendNickName(imGroupMessageTxEvent.getSendNickName());
        groupMessage.setSendTime(imGroupMessageTxEvent.getSendTime());
        groupMessage.setContent(imGroupMessageTxEvent.getGroupMessageDTO().getContent());
        groupMessage.setType(imGroupMessageTxEvent.getGroupMessageDTO().getType());
        groupMessage.setStatus(MessageStatus.UNSEND.code());
        if (CollectionUtil.isNotEmpty(imGroupMessageTxEvent.getGroupMessageDTO().getAtUserIds())) {
            groupMessage.setAtUserIds(StrUtil.join(",", imGroupMessageTxEvent.getGroupMessageDTO().getAtUserIds()));
        }
        return this.saveOrUpdate(groupMessage);
    }

    @Override
    public boolean checkExists(Long messageId) {
        return baseMapper.checkExists(messageId) != null;
    }

    @Override
    public List<GroupMessageVO> getUnreadGroupMessageList(Long groupId, Date sendTime, Long sendId, Integer status, Long maxReadId, Integer limitCount) {
        return baseMapper.getUnreadGroupMessageList(groupId, sendTime, sendId, status, maxReadId, limitCount);
    }

    @Override
    public List<GroupMessageVO> loadGroupMessageList(Long minId, Date minDate, List<Long> ids, Integer status, Integer limitCount) {
        return baseMapper.loadGroupMessageList(minId, minDate, ids, status, limitCount);
    }

    @Override
    public List<GroupMessageVO> getHistoryMessage(Long groupId, Date sendTime, Integer status, long stIdx, long size) {
        return baseMapper.getHistoryMessage(groupId, sendTime, status, stIdx, size);
    }

    @Override
    public Long getMaxMessageId(Long groupId) {
        return baseMapper.getMaxMessageId(groupId);
    }

    @Override
    public GroupMessageVO getGroupMessageById(Long messageId) {
        return baseMapper.getGroupMessageByI(messageId);
    }

    @Override
    public int updateStatus(Integer status, Long messageId) {
        return baseMapper.updateStatus(status, messageId);
    }
}
