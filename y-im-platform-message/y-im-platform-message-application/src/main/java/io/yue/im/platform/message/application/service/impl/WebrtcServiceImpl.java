package io.yue.im.platform.message.application.service.impl;

import cn.hutool.core.util.StrUtil;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.model.IMPrivateMessage;
import io.yue.im.common.domain.model.IMUserInfo;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.enums.MessageType;
import io.yue.im.platform.common.model.vo.PrivateMessageVO;
import io.yue.im.platform.common.session.SessionContext;
import io.yue.im.platform.common.session.UserSession;
import io.yue.im.platform.common.session.WebrtcSession;
import io.yue.im.platform.message.application.ice.ICEServer;
import io.yue.im.platform.message.application.ice.ICEServerConfig;
import io.yue.im.platform.message.application.service.WebrtcService;
import io.yue.im.sdk.client.IMClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 */
@Service
public class WebrtcServiceImpl implements WebrtcService {
    @Autowired
    private IMClient imClient;
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private ICEServerConfig iceServerConfig;

    @Override
    public void call(Long uid, String offer) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        if (!imClient.isOnline(uid)) {
            throw new IMException("对方目前不在线");
        }
        // 创建webrtc会话
        WebrtcSession webrtcSession = new WebrtcSession();
        webrtcSession.setCallerId(session.getUserId());
        webrtcSession.setCallerTerminal(session.getTerminal());
        String key = getSessionKey(session.getUserId(), uid);
        distributedCacheService.set(key, webrtcSession, IMPlatformConstants.WEBRTC_SESSION_CACHE_EXPIRE, TimeUnit.HOURS);
        // 向对方所有终端发起呼叫
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_CALL.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(offer);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void cancel(Long uid) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        // 删除会话信息
        this.removeWebrtcSession(session.getUserId(), uid);
        // 向对方所有终端推送取消通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_ACCEPT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void failed(Long uid, String reason) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        this.removeWebrtcSession(uid, session.getUserId());
        // 向发起方推送通话失败信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_FAILED.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        // 告知其他终端已经会话失败,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setReceiveTerminals(Collections.singletonList(webrtcSession.getCallerTerminal()));
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void accept(Long uid, @RequestBody String answer) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        if (uid == null || StrUtil.isEmpty(answer)){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 更新接受者信息
        webrtcSession.setAcceptorId(session.getUserId());
        webrtcSession.setAcceptorTerminal(session.getTerminal());
        String key = getSessionKey(session.getUserId(), uid);
        distributedCacheService.set(key, webrtcSession, IMPlatformConstants.WEBRTC_SESSION_CACHE_EXPIRE, TimeUnit.HOURS);
        // 向发起人推送接受通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_ACCEPT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(answer);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        // 告知其他终端已经接受会话,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setReceiveTerminals((Collections.singletonList(webrtcSession.getCallerTerminal())));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void reject(Long uid) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 向发起人推送拒绝通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_REJECT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        // 告知其他终端已经拒绝会话,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setReceiveTerminals(Collections.singletonList(webrtcSession.getCallerTerminal()));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void leave(Long uid) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 向对方推送挂断通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_HANDUP.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        Integer terminal = getTerminalType(uid, webrtcSession);
        sendMessage.setReceiveTerminals(Collections.singletonList(terminal));
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void candidate(Long uid, String candidate) {
        if (uid == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 向发起方推送同步candidate信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_CANDIDATE.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(candidate);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(),session.getTerminal()));
        sendMessage.setReceiveId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        Integer terminal = getTerminalType(uid, webrtcSession);
        sendMessage.setReceiveTerminals(Collections.singletonList(terminal));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public List<ICEServer> getIceServers() {
        return iceServerConfig.getIceServers();
    }

    private WebrtcSession getWebrtcSession(Long userId, Long uid) {
        String key = getSessionKey(userId, uid);
        WebrtcSession webrtcSession = distributedCacheService.getObject(key, WebrtcSession.class);
        if (webrtcSession == null) {
            throw new IMException("视频通话已结束");
        }
        return webrtcSession;
    }

    private void removeWebrtcSession(Long userId, Long uid) {
        String key = getSessionKey(userId, uid);
        distributedCacheService.delete(key);
    }

    private String getSessionKey(Long id1, Long id2) {
        Long minId = id1 > id2 ? id2 : id1;
        Long maxId = id1 > id2 ? id1 : id2;
        return String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_WEBRTC_SESSION, minId.toString(), maxId.toString());
    }

    private Integer getTerminalType(Long uid, WebrtcSession webrtcSession) {
        if (uid.equals(webrtcSession.getCallerId())) {
            return webrtcSession.getCallerTerminal();
        }
        return webrtcSession.getAcceptorTerminal();
    }
}
