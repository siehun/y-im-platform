package io.yue.im.platform.message.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.cache.id.SnowFlakeFactory;
import io.yue.im.common.cache.time.SystemClock;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.model.IMGroupMessage;
import io.yue.im.common.domain.model.IMUserInfo;
import io.yue.im.common.mq.MessageSenderService;
import io.yue.im.common.mq.event.MessageEventSenderService;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.dto.GroupMessageDTO;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.enums.MessageStatus;
import io.yue.im.platform.common.model.enums.MessageType;
import io.yue.im.platform.common.model.params.GroupParams;
import io.yue.im.platform.common.model.vo.GroupMemberSimpleVO;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.platform.common.session.SessionContext;
import io.yue.im.platform.common.session.UserSession;
import io.yue.im.platform.common.threadpool.GroupMessageThreadPoolUtils;
import io.yue.im.platform.common.utils.DateTimeUtils;
import io.yue.im.platform.dubbo.group.GroupDubboService;
import io.yue.im.platform.message.application.service.GroupMessageService;
import io.yue.im.platform.message.domain.event.IMGroupMessageTxEvent;
import io.yue.im.platform.message.domain.service.GroupMessageDomainService;
import io.yue.im.sdk.client.IMClient;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 群聊消息服务实现类
 */
@Service
public class GroupMessageServiceImpl implements GroupMessageService {
    private final Logger logger = LoggerFactory.getLogger(GroupMessageServiceImpl.class);
    @Autowired
    private MessageSenderService messageSenderService;
    @Autowired
    private GroupMessageDomainService groupMessageDomainService;
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private GroupDubboService groupDubboService;
    @Autowired
    private MessageEventSenderService messageEventSenderService;
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private IMClient imClient;

    @Override
    public Long sendMessage(GroupMessageDTO dto) {
        UserSession userSession = SessionContext.getSession();
        boolean isExists = groupDubboService.isExists(dto.getGroupId());
        if (!isExists){
            throw new IMException(HttpCode.PROGRAM_ERROR, "群组不存在或者已经解散");
        }
        //当前用户是否在群组中
        GroupMemberSimpleVO groupMemberSimpleVO = groupDubboService.getGroupMemberSimpleVO(new GroupParams(userSession.getUserId(), dto.getGroupId()));
        if (Objects.isNull(groupMemberSimpleVO) || groupMemberSimpleVO.getQuit()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您已不在群聊里面，无法发送消息");
        }
        //获取群组中的群成员列表
        List<Long> userIds = groupDubboService.getUserIdsByGroupId(dto.getGroupId());
        if (CollectionUtil.isEmpty(userIds)){
            userIds = Collections.emptyList();
        }
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !userSession.getUserId().equals(id)).collect(Collectors.toList());
        //消息id
        Long messageId = SnowFlakeFactory.getSnowFlakeFromCache().nextId();
        //构造事务事件
        IMGroupMessageTxEvent imGroupMessageTxEvent = new IMGroupMessageTxEvent(messageId,
                userSession.getUserId(),
                groupMemberSimpleVO.getAliasName(),
                userSession.getTerminal(),
                new Date(),
                IMPlatformConstants.TOPIC_GROUP_TX_MESSAGE,
                userIds,
                dto);
//        TransactionSendResult sendResult = messageSenderService.sendMessageInTransaction(imGroupMessageTxEvent, null);
        TransactionSendResult sendResult = messageEventSenderService.sendMessageInTransaction(imGroupMessageTxEvent, null);
        if (sendResult.getSendStatus() != SendStatus.SEND_OK){
            logger.error("PrivateMessageServiceImpl|发送事务消息失败|参数:{}", JSONObject.toJSONString(dto));
        }
        return messageId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveIMGroupMessageTxEvent(IMGroupMessageTxEvent imGroupMessageTxEvent) {
        return groupMessageDomainService.saveIMGroupMessageTxEvent(imGroupMessageTxEvent);
    }

    @Override
    public boolean checkExists(Long messageId) {
        return groupMessageDomainService.checkExists(messageId);
    }

    @Override
    public void pullUnreadMessage() {
        UserSession session = SessionContext.getSession();
        List<GroupMemberSimpleVO> groupMemberList = groupDubboService.getGroupMemberSimpleVOList(session.getUserId());
        if (CollectionUtil.isEmpty(groupMemberList)){
            return;
        }
        groupMemberList.parallelStream().forEach((member) -> {
            String key = String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_GROUP_READED_POSITION, member.getGroupId().toString(), session.getUserId().toString());
            String maxReadedIdStr = distributedCacheService.get(key);
            Long maxReadedId = StrUtil.isEmpty(maxReadedIdStr) ? 0L : Long.parseLong(maxReadedIdStr);
            List<GroupMessageVO> unreadGroupMessageList = groupMessageDomainService.getUnreadGroupMessageList(member.getGroupId(), member.getCreatedTime(),
                    session.getUserId(), MessageStatus.RECALL.code(), maxReadedId, IMPlatformConstants.PULL_HISTORY_MESSAGE_LIMIT_COUNR);
            if (!CollectionUtil.isEmpty(unreadGroupMessageList)){
                GroupMessageThreadPoolUtils.execute(() -> {
                    for (GroupMessageVO message : unreadGroupMessageList){
                        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                        // 只推给自己当前终端
                        sendMessage.setReceiveIds(Collections.singletonList(session.getUserId()));
                        sendMessage.setReceiveTerminals(Collections.singletonList(session.getTerminal()));
                        sendMessage.setData(message);
                        imClient.sendGroupMessage(sendMessage);
                    }
                });
                // 发送消息
                logger.info("拉取未读群聊消息，用户id:{},群聊id:{},数量:{}", session.getUserId(), member.getGroupId(), unreadGroupMessageList.size());
            }
        });
    }

    @Override
    public List<GroupMessageVO> loadMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        List<Long> ids = groupDubboService.getGroupIdsByUserId(session.getUserId());
        if (CollectionUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        // 只能拉取最近1个月的
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        List<GroupMessageVO> groupMessageList = groupMessageDomainService.loadGroupMessageList(minId, minDate, ids,
                MessageStatus.RECALL.code(), IMPlatformConstants.PULL_HISTORY_MESSAGE_LIMIT_COUNR);
        if (CollectionUtil.isEmpty(groupMessageList)){
            return Collections.emptyList();
        }
        List<GroupMessageVO> vos = groupMessageList.stream().peek(m -> {
            // 被@用户列表
            List<String> atIds = Arrays.asList(StrUtil.split(m.getAtUserIdsStr(), IMConstants.USER_ID_SPLIT));
            m.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
        }).collect(Collectors.toList());
        // 消息状态,数据库没有存群聊的消息状态，需要从redis取
        List<String> keys = ids.stream().map(id -> String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_GROUP_READED_POSITION,
                        id.toString(), session.getUserId().toString())).collect(Collectors.toList());
        List<String> sendPos = distributedCacheService.multiGet(keys);
        for (int idx = 0; idx < ids.size(); idx ++){
            Long id = ids.get(idx);
            String str = sendPos.get(idx);
            Long sendMaxId = StrUtil.isEmpty(str) ? 0L : Long.parseLong(str);
            vos.stream().filter(vo -> vo.getGroupId().equals(id)).forEach(vo -> {
                if (vo.getId() <= sendMaxId) {
                    // 已读
                    vo.setStatus(MessageStatus.READED.code());
                } else {
                    // 未推送
                    vo.setStatus(MessageStatus.UNSEND.code());
                }
            });
        }
        return vos;
    }

    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : IMPlatformConstants.DEFAULT_PAGE;
        size = size > 0 ? size : IMPlatformConstants.DEFAULT_PAGE_SIZE;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        GroupMemberSimpleVO groupMember = groupDubboService.getGroupMemberSimpleVO(new GroupParams(userId, groupId));
        if (groupMember == null || groupMember.getQuit()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        List<GroupMessageVO> historyMessage = groupMessageDomainService.getHistoryMessage(groupId, groupMember.getCreatedTime(), MessageStatus.RECALL.code(), stIdx, size);
        if (CollectionUtil.isEmpty(historyMessage)){
            historyMessage = Collections.emptyList();
        }
        logger.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, historyMessage.size());
        return historyMessage;
    }

    @Override
    public void readedMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        Long maxMessageId = groupMessageDomainService.getMaxMessageId(groupId);
        if (maxMessageId == null){
            return;
        }
        // 推送消息给自己的其他终端
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
        // 记录已读消息位置
        String key = StrUtil.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_GROUP_READED_POSITION, groupId, session.getUserId());
        distributedCacheService.set(key, String.valueOf(maxMessageId));
    }

    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msg = groupMessageDomainService.getGroupMessageById(id);
        if (Objects.isNull(msg)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (SystemClock.millisClock().now() - msg.getSendTime().getTime() > IMConstants.ALL_RECALL_SECONDS * 1000) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        GroupMemberSimpleVO member = groupDubboService.getGroupMemberSimpleVO(new GroupParams(session.getUserId(), msg.getGroupId()));
        if (Objects.isNull(member) || member.getQuit()) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您已不在群里，无法撤回消息");
        }
        //更新消息状态
        groupMessageDomainService.updateStatus(MessageStatus.RECALL.code(), id);
        GroupMessageThreadPoolUtils.execute(() -> {
            List<Long> userIds = groupDubboService.getUserIdsByGroupId(msg.getGroupId());
            // 不用发给自己
            userIds = userIds.stream().filter(uid -> !session.getUserId().equals(uid)).collect(Collectors.toList());
            msg.setType(MessageType.RECALL.code());
            String content = String.format("'%s'撤回了一条消息", member.getAliasName());
            msg.setContent(content);
            msg.setSendTime(new Date());

            IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
            sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
            sendMessage.setReceiveIds(userIds);
            sendMessage.setData(msg);
            sendMessage.setSendResult(false);
            sendMessage.setSendToSelf(false);
            imClient.sendGroupMessage(sendMessage);

            // 推给自己其他终端
            msg.setContent("你撤回了一条消息");
            sendMessage.setSendToSelf(true);
            sendMessage.setReceiveIds(Collections.emptyList());
            sendMessage.setReceiveTerminals(Collections.emptyList());
            imClient.sendGroupMessage(sendMessage);
            logger.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
        });
    }
}
